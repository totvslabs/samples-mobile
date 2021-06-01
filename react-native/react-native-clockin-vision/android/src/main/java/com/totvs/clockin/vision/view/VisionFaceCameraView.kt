package com.totvs.clockin.vision.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.view.core.CameraViewModuleOptions
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.DetectionAnalyzer
import com.totvs.camera.vision.core.VisionModuleOptions
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.FastFaceDetector
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.*
import com.totvs.camera.vision.utils.exclusiveUse
import com.totvs.clockin.vision.core.ClockInVisionModuleOptions
import com.totvs.clockin.vision.core.ModelOutput
import com.totvs.clockin.vision.core.RecognitionModel
import com.totvs.clockin.vision.face.*
import com.totvs.clockin.vision.face.VisionFaceCamera.RecognitionOptions
import com.totvs.clockin.vision.face.VisionFaceCamera.RecognitionResult
import com.totvs.clockin.vision.lifecycle.ReactLifecycleOwner
import com.totvs.clockin.vision.utils.createFile
import com.totvs.clockin.vision.utils.toBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Camera View capable of face detection.
 */
class VisionFaceCameraView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : CameraView(context, attrs, style), VisionFaceCamera {

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

    /**
     * Flag to determine when the camera is busy processing one image for recognition.
     */
    private val isRecognizing = AtomicBoolean(false)

    /**
     * Handy getter to get the real installed analyzer.
     */
    private val detectorAnalyzer by lazy {
        DetectionAnalyzer(detectionExecutor, FastFaceDetector(context))
    }

    /**
     * Detector dedicated to still captures detection
     */
    private val stillCapturesDetector by lazy {
        FastFaceDetector(context)
    }

    init {
        enableDebug()
        enableStillCapturesDetection()
        startUp()
    }

    // [FaceVisionCamera] contract
    override var overlayGraphicsColor: String = "#FFFFFF"
        set(value) {
            field = value
            faceGraphic.setLandmarksColor(value)
        }

    override var liveness: Liveness? = null
        set(value) {
            field = value
            // if we got a liveness installed then we enable the detector, otherwise we
            // check if we need to uninstall it.
            value?.let {
                enableLiveness(it)
            } ?: checkAnalyzerState()
        }

    override var proximity: Proximity? = null
        set(value) {
            field = value
            // if we got a proximity installed then we enable the detector, otherwise we
            // don't event enable the detector.
            value?.let {
                enableProximity(it)
            } ?: checkAnalyzerState()
        }

    override val isLivenessEnabled: Boolean
        get() = null != liveness


    override val isFaceProximityEnabled: Boolean
        get() = null != proximity

    /**
     * Let's optimize for capture size. Sizes that proved to be good fit are:
     *
     * 1. kiosk: 960x720, 640x480(preferred)
     */
    override fun desiredPreviewSize(): Size? = if (isDisplayPortrait)
        Size(480, 640)
    else
        Size(640, 480)

    /**
     * Capture an still picture and recognizes the person on it.
     *
     * [options] is used to control the behavior of the recognition task. if [options.saveImage]
     * is set to true we'll save the picture at [options.outputDir] otherwise we'll skip that
     * task.
     */
    override fun recognizeStillPicture(
        options: RecognitionOptions,
        onResult: (RecognitionResult) -> Unit
    ) = ensureSetup {
        if (isRecognizing.get()) {
            if (isDebug) {
                Log.w(TAG, "Recognizer is busy, ignoring this request.")
            }
            return@ensureSetup
        }
        isRecognizing.set(true)

        takePicture { image, throwable ->
            throwable?.let {
                // on error: reset
                isRecognizing.set(false)
                Log.e(TAG, "Error taking picture", it)
            }

            val bitmap = when (detectStillCaptures) {
                // will be closed after detection.
                true -> image?.exclusiveUse {
                    it.image?.toBitmap(it.imageInfo.rotationDegrees)
                }
                // closing the image after using.
                else -> image?.use {
                    it.image?.toBitmap(it.imageInfo.rotationDegrees)
                }
            }

            if (null != bitmap) {
                // we add an extra task if we're required to save the image.
                val latch = CountDownLatch(1 + if (options.saveImage) 1 else 0)
                val result = RecognitionResult()

                if (options.saveImage) {
                    val saver = ImageSaver(bitmap, options) { file, exception ->
                        exception?.let {
                            Log.e(TAG, "Error saving image", it)
                        }
                        result.file = file
                        // notify we're done
                        latch.countDown()
                    }
                    recognitionExecutor.execute(saver)
                }
                val recognizer = ImageRecognizer(
                    bitmap = bitmap,
                    includeDetection = detectStillCaptures,
                    executor = detectionExecutor,
                    detector = stillCapturesDetector,
                    image = if (detectStillCaptures) image else null,
                    onDetected = {
                        // trigger safe close, only called if image is provided as non-null, but
                        // just in case: conditionally take.
                        image?.takeIf { detectStillCaptures }?.use {  }
                    },
                    onRecognized = { output, exception ->
                        exception?.let {
                            Log.e(TAG, "Error recognizing image", it)
                        }
                        result.output = output
                        // notify we're done
                        latch.countDown()
                    }
                )
                recognitionExecutor.execute(recognizer)

                try {
                    latch.await()
                } catch (ex: Exception) {
                    if (isDebug) {
                        Log.e(TAG, "Closing [ImageRecognizer] & [ImageSaver]")
                    }
                }
                isRecognizing.set(false)
                // we send to process the result and allow the recognizer to receive more requests.
                onResult(result)
            }
        }
    }

    /**
     * Let's setup the view with an appropriate model and options
     */
    override fun setup(model: RecognitionModel<Bitmap, Face>) {
        this.model = model
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
        setupExecutors()
        bindTo(lifecycleOwner)
        isReady.set(true)
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
        if (null != analyzer) {
            return
        }

        if (isDebug) {
            Log.i(TAG, "Installing DetectionAnalyzer...")
        }
        detectorAnalyzer.enable(FastFaceDetector)

        analyzer = detectorAnalyzer
    }

    /**
     * Check if we need to uninstall the analyzer
     */
    private fun checkAnalyzerState() {
        if (null == liveness) {
            disableLiveness()
        }
        if (null == proximity) {
            disableProximity()
        }
        if (null == proximity && null == liveness) {
            detectorAnalyzer.disable(FastFaceDetector)
            analyzer = null
        }
    }

    /**
     * Bind lifecycle owner
     */
    private fun bindTo(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.apply {
            removeObserver(lifecycleObserver)
            addObserver(lifecycleObserver)
        }
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

        // two threads for analyzer another for stillCapturesDetector
        detectionExecutor = Executors.newFixedThreadPool(3)
        // one for [ImageSaver] another for [ImageRecognizer]
        recognitionExecutor = Executors.newFixedThreadPool(2)
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
            Log.i(TAG, "Enabling liveness $liveness. Analyzer is ready: ${null != analyzer}")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()

        // closing current connection
        livenessConnection?.disconnect()

        // setup the vision stream for face detected objects.
        livenessConnection = if (liveness is LivenessFace) {
            detectorAnalyzer
                .detections
                .filterIsInstance<FaceObject>()
                .transform(faceNoseTranslator)
                .connect(liveness)
        } else {
            detectorAnalyzer
                .detections
                .filterIsInstance<FaceObject>()
                .connect(liveness)
        }

        installFaceGraphics(liveness)
    }

    /**
     * Turn down liveness connections and graphics
     */
    private fun disableLiveness() {
        if (isDebug) {
            Log.i(TAG, "Disabling liveness")
        }
        livenessConnection?.disconnect()
        // uninstall all face graphics
        clearFaceGraphics()
    }

    /**
     * Enable the proximity feature on this vision camera view.
     */
    private fun enableProximity(proximity: Proximity) {
        if (isDebug) {
            Log.i(TAG, "Enabling proximity $proximity. Analyzer is ready: ${null != analyzer}")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()

        // closing current connection
        proximityConnection?.disconnect()

        // setup the vision stream for face detected objects.
        proximityConnection = detectorAnalyzer
            .detections
            .filterIsInstance<FaceObject>()
            .transform(faceBoundsScaler)
            .connect(proximity)
    }

    /**
     * Turn down liveness connections and graphics
     */
    private fun disableProximity() {
        if (isDebug) {
            Log.i(TAG, "Disabling proximity")
        }
        proximityConnection?.disconnect()
    }

    /**
     * Enable graphic overlay on the camera
     */
    private fun installFaceGraphics(liveness: Liveness) {
        if (isDebug) {
            Log.i(TAG, "Enabling face graphics. Analyzer is ready: ${null != analyzer}")
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
            detectorAnalyzer
                .detections
                .filterIsInstance<FaceObject>()
                .sendOn(ContextCompat.getMainExecutor(context))
                .transform(AnimateEyes())
                .connect(faceGraphic)
        } else {
            detectorAnalyzer
                .detections
                .filterIsInstance<FaceObject>()
                .sendOn(ContextCompat.getMainExecutor(context))
                .transform(AnimateNose())
                .connect(faceGraphic)
        }
    }

    /**
     * Clear face graphics and remove the graphic from overlay
     */
    private fun clearFaceGraphics() {
        // clear the face graphics.
        faceGraphic.clear()
        // clear every object on the graphic overlay
        graphicOverlay.clear()
    }


    private fun ensureSetup(block: () -> Unit) {
        if (!::model.isInitialized) {
            throw IllegalStateException("FaceVisionCameraView haven't been setup. Please call setup first")
        }
        return block()
    }

    /**
     * Task to save the image to disk.
     */
    private inner class ImageSaver(
        private val bitmap: Bitmap,
        private val options: RecognitionOptions,
        private val onSave: (File?, Throwable?) -> Unit
    ) : Runnable {
        override fun run() {
            val location = createFile(context, options.outputDir)
            val output = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            try {
                output.use { byteArray ->
                    FileOutputStream(location).use { out ->
                        out.write(byteArray.toByteArray())
                        out.flush()
                    }
                }
                onSave(location, null)
            } catch (ex: Exception) {
                onSave(null, ex)
            }
        }
    }

    /**
     * Task to perform the recognition task on the provided image.
     */
    private inner class ImageRecognizer(
        private val bitmap: Bitmap,
        private val includeDetection: Boolean,
        private val detector: AbstractVisionDetector<FaceObject>,
        private val executor: Executor,
        private val image: ImageProxy?,
        private val onDetected: (FaceObject) -> Unit,
        private val onRecognized: (ModelOutput<Face>, Throwable?) -> Unit
    ) : Runnable {
        override fun run() {
            try {
                image?.takeIf { includeDetection }
                    ?.exclusiveUse {
                        detector.detect(executor, image = it, onDetected = { face ->
                            onDetected(face)
                            recognize(face)
                        })
                    }
                    ?: model.recognize(bitmap) { result ->
                        onRecognized(result, null)
                    }
            } catch (ex: Exception) {
                onRecognized(ModelOutput(), ex)
            }
        }

        private fun recognize(face: FaceObject) {
            try {
                with(bitmap.getDetectionMeta(face)) {
                    model.recognize(bitmap, includeDetection = !skip) { result ->
                        onRecognized(result, null)
                    }
                }
            } catch (ex: Exception) {
                onRecognized(ModelOutput(), ex)
            }
        }

        /**
         * Determine detection meta info based on the size of the face detected.
         * This is related to the inability of the cpp lib to perform well on images
         * with high light source.
         */
        private fun Bitmap.getDetectionMeta(face: FaceObject): DetectionMeta {
            if (face === NullFaceObject) {
                return DetectionMeta(skip = false, bitmap = this)
            }

            var x = max(face.boundingBox?.left?.toInt() ?: 0, 0)
            var y = max(face.boundingBox?.top?.toInt() ?: 0, 0)

            val cropWidth = when (x + face.width > width) {
                true -> width.also { x = 0 }
                else -> face.width.toInt()
            }

            val cropHeight = when (y + face.height > height) {
                true -> height.also { y = 0 }
                else -> face.height.toInt()
            }

            return when (cropWidth != face.width.toInt() || cropHeight != face.height.toInt()) {
                true -> DetectionMeta(
                    skip = true,
                    bitmap = Bitmap.createBitmap(this, x, y, cropWidth, cropHeight)
                )
                else -> DetectionMeta(skip = false, bitmap = this)
            }
        }

        private inner class DetectionMeta(val skip: Boolean, val bitmap: Bitmap)
    }

    companion object {
        private const val TAG = "FaceVisionCameraView"

        private val isDebug get() = ClockInVisionModuleOptions.DEBUG_ENABLED
        private val detectStillCaptures get() = ClockInVisionModuleOptions.USES_STILL_CAPTURES_DETECTION

        // use this to debug the whole set of libraries. comment then out if no needed.
        private fun enableDebug() {
            VisionModuleOptions.DEBUG_ENABLED = true
            CameraViewModuleOptions.DEBUG_ENABLED = true
            ClockInVisionModuleOptions.DEBUG_ENABLED = true
        }

        /**
         * Enable still capture detection during recognitions.
         *
         * This is related to the inability of the cpp lib to perform well on images
         * with high light source. To disable this behavior remove the call to this method above.
         */
        private fun enableStillCapturesDetection() {
            ClockInVisionModuleOptions.USES_STILL_CAPTURES_DETECTION = true
        }
    }
}