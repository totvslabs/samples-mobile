package com.totvs.clockin.vision.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.core.CameraViewModuleOptions
import com.totvs.camera.vision.DetectionAnalyzer
import com.totvs.camera.vision.core.VisionModuleOptions
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.FastFaceDetector
import com.totvs.camera.vision.stream.*
import com.totvs.clockin.vision.core.ClockInVisionModuleOptions
import com.totvs.clockin.vision.core.RecognitionModel
import com.totvs.clockin.vision.face.*
import com.totvs.clockin.vision.face.FaceVisionCamera.RecognitionOptions
import com.totvs.clockin.vision.lifecycle.ReactLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import com.totvs.camera.view.GraphicOverlay

/**
 * Camera View capable of face detection.
 */
class FaceVisionCameraView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : CameraView(context, attrs, style), FaceVisionCamera {

    // Listeners
    /**
     * Bridge lifecycle event listeners.
     */
    private object LifecycleEvents : LifecycleEventListener {
        override fun onHostResume() = ReactLifecycleOwner.onHostResume()
        override fun onHostPause() = ReactLifecycleOwner.onHostPause()
        override fun onHostDestroy() = ReactLifecycleOwner.onHostDestroy()
    }

    /**
     * lifecycle observer to control be notified when the host is destroyed.
     */
    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(lifecycle: LifecycleOwner) {
            tearDown()
        }
    }

    /**
     * Lifecycle owner.
     *
     * This property is a hack around react native environment. Since
     * react native is built on top of old android activity api, it is not
     * lifecycle aware, hence this view context is not a lifecycle owner.
     *
     * We create a custom lifecycle owner tied to react-native self lifecycle
     * when we detect that this view context is not a lifecycle owner, otherwise
     * a custom one is used.
     *
     * This view obtain its lifecycle events from react-native mapping lifecycle events
     * when running on a react-native environment, otherwise through a subscription
     * to the holder context.
     *
     * @see also [ReactLifecycleOwner]
     */
    private val lifecycleOwner: LifecycleOwner = when (context) {
        is LifecycleOwner -> context
        is ThemedReactContext -> ReactLifecycleOwner
            .also {
                context.addLifecycleEventListener(LifecycleEvents)
            }
        else -> throw IllegalArgumentException(
            """ Invalid context type. You must use this view with a LifecycleOwner 
                or ThemedReactContext context 
            """.trimIndent()
        )
    }

    /**
     * Executor for detection tasks.
     */
    private lateinit var detectionExecutor: ExecutorService

    /**
     * Executor for recognition tasks
     */
    private lateinit var recognitionExecutor: ExecutorService

    /**
     * Model to be used for recognition
     */
    private lateinit var model: RecognitionModel<Bitmap, Face>

    /**
     * Options to be used with this camera view
     */
    private lateinit var options: FaceVisionOptions

    /**
     * [Connection] of liveness feature
     */
    private var livenessConnection: Connection? = null

    /**
     * [Connection] of proximity feature
     */
    private var proximityConnection: Connection? = null

    /**
     * [Connection] of face graphics
     */
    private var graphicsConnection: Connection? = null

    /**
     * Graphic to record graphics.
     */
    private val faceGraphic = FaceGraphic(context)

    /**
     * Whether or not we're on a ready state.
     */
    private val isReady = AtomicBoolean(false)

    /**
     * [Transformer] that adjust face objects source bounds to the [GraphicOverlay]
     * coordinate system.
     */
    private val faceBoundsScaler by lazy {
        FaceBoundsScaler(graphicOverlay)
    }

    /**
     * [Transformer] that scale nose landmark from the source coordinate into [GraphicOverlay]
     * coordinate system.
     */
    private val faceNoseTranslator by lazy {
        FaceNoseTranslator(graphicOverlay)
    }

    init {
        enableDebug()
        startUp()
    }

    // [FaceVisionCamera] contract
    override var liveness: Liveness? = null
        set(value) {
            field = value
            value?.let {
                enableLiveness(it)
            }
        }

    override var proximity: Proximity? = null
        set(value) {
            field = value
            value?.let {
                enableProximity(it)
            }
        }

    override val isLivenessEnabled: Boolean
        get() = null != liveness


    override val isFaceProximityEnabled: Boolean
        get() = null != proximity


    override fun recognizeStillPicture(options: RecognitionOptions) = ensureSetup {

    }

    /**
     * Let's setup the view with an appropriate model and options
     */
    override fun setup(model: RecognitionModel<Bitmap, Face>, options: FaceVisionOptions) {
        this.model = model
        this.options = options
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startUp()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tearDown()
    }

    /**
     * Setup every requirement of this face vision camera
     */
    private fun startUp() {
        if (isReady.get()) return

        if (isDebug) {
            Log.e(TAG, "Setting up camera requirements")
        }
        // start executors
        setupExecutors()
        // install detections
        installAnalyzer()
        // bind to lifecycle
        bindTo(lifecycleOwner)
    }

    /**
     * Turn down every requirement of this face vision camera
     */
    private fun tearDown() {
        isReady.set(false)

        if (isDebug) {
            Log.e(TAG, "Tear down camera requirements")
        }
        tearDownExecutors()
        closeConnections()
    }

    /**
     * Install detection analyzer on [CameraView]
     */
    private fun installAnalyzer() {
        analyzer = DetectionAnalyzer(
            detectionExecutor,
            FastFaceDetector(context)
        ).apply {
            // disable(/*any detector here*/)
        }
    }

    /**
     * Bind lifecycle owner
     */
    private fun bindTo(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    /**
     * Disconnect all [VisionStream] connections
     */
    private fun closeConnections() {
        livenessConnection?.disconnect()
        proximityConnection?.disconnect()
        graphicsConnection?.disconnect()
    }

    /**
     * Setup the executors for background jobs
     */
    private fun setupExecutors() {
        if (::detectionExecutor.isInitialized && !detectionExecutor.isShutdown) {
            return // this suffix to not initialize more than once the executors
        }
        detectionExecutor = Executors.newSingleThreadExecutor()
        recognitionExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Terminate all executors
     */
    private fun tearDownExecutors() {
        if (!::detectionExecutor.isInitialized) {
            return
        }
        detectionExecutor.shutdownNow()
        recognitionExecutor.shutdownNow()
    }

    /**
     * Enable the liveness feature in this vision camera view
     */
    private fun enableLiveness(liveness: Liveness) {
        if (isDebug) {
            Log.e(TAG, "Enabling liveness $liveness. Analyzer is ready: ${null != analyzer}")
        }
        // closing current connection
        livenessConnection?.disconnect()

        // setup the vision stream for face detected objects.
        livenessConnection = if(liveness is LivenessFace) {
            (analyzer as? DetectionAnalyzer)
                ?.detections
                ?.filterIsInstance<FaceObject>()
                ?.transform(faceNoseTranslator)
                ?.connect(liveness)
        } else {
            (analyzer as? DetectionAnalyzer)
                ?.detections
                ?.filterIsInstance<FaceObject>()
                ?.connect(liveness)
        }

        installFaceGraphics(liveness)
    }

    /**
     * Enable the proximity feature on this vision camera view.
     */
    private fun enableProximity(proximity: Proximity) {
        if (isDebug) {
            Log.e(TAG, "Enabling proximity $proximity. Analyzer is ready: ${null != analyzer}")
        }
        // closing current connection
        proximityConnection?.disconnect()

        // setup the vision stream for face detected objects.
        proximityConnection = (analyzer as? DetectionAnalyzer)
            ?.detections
            ?.filterIsInstance<FaceObject>()
            ?.transform(faceBoundsScaler)
            ?.connect(proximity)
    }

    /**
     * Enable graphic overlay on the camera
     */
    private fun installFaceGraphics(liveness: Liveness) {
        if (isDebug) {
            Log.e(TAG, "Enabling face graphics. Analyzer is ready: ${null != analyzer}")
        }
        // clear the face graphics.
        faceGraphic.clear()
        // clear every object on the graphic overlay
        graphicOverlay.clear()
        // re-add the face graphic overlay
        graphicOverlay.add(faceGraphic)
        // closing current connection
        graphicsConnection?.disconnect()
        // we don't need to draw nose under liveness eyes nor eyes on liveness face.
        faceGraphic.apply {
            drawEyes = liveness is LivenessEyes
            drawNose = liveness is LivenessFace
        }

        // setup the vision stream for face detected objects.
        graphicsConnection = if (liveness is LivenessEyes) {
            (analyzer as? DetectionAnalyzer)
                ?.detections
                ?.filterIsInstance<FaceObject>()
                ?.sendOn(ContextCompat.getMainExecutor(context))
                ?.transform(AnimateEyes())
                ?.connect(faceGraphic)
        } else {
            (analyzer as? DetectionAnalyzer)
                ?.detections
                ?.filterIsInstance<FaceObject>()
                ?.sendOn(ContextCompat.getMainExecutor(context))
                ?.transform(AnimateNose())
                ?.connect(faceGraphic)
        }
    }


    private fun <T> ensureSetup(block: () -> T): T {
        if (!::model.isInitialized || !::options.isInitialized) {
            throw IllegalStateException("FaceVisionCameraView haven't been setup. Please call setup first")
        }
        return block()
    }

    companion object {
        private const val TAG = "FaceVisionCameraView"

        private val isDebug get() = ClockInVisionModuleOptions.DEBUG_ENABLED

        // use this to debug the whole set of libraries. comment then out if no needed.
        private fun enableDebug() {
            VisionModuleOptions.DEBUG_ENABLED = true
            CameraViewModuleOptions.DEBUG_ENABLED = true
            ClockInVisionModuleOptions.DEBUG_ENABLED = true
        }
    }
}