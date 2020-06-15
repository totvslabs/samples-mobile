package com.totvs.camera.view

import android.Manifest.permission
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.annotation.FloatRange
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.annotation.experimental.UseExperimental
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.totvs.camera.core.*
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.core.ImageProxyImpl
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This controller is backed fully by [CameraX] implementation, in order to change the
 * source of the camera device, this is the class that needs to be checked for.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CameraSource(private val view: CameraView) {

    init {
        @Suppress("MissingPermission")
        with(ProcessCameraProvider.getInstance(view.context)) {
            addListener(Runnable {
                cameraProvider = get()

                // if at this point we know the lifecycle, we bind to it
                currentLifecycle?.let {
                    bindToLifecycle(it)
                }
            }, ContextCompat.getMainExecutor(view.context))
        }
    }

    // Computed properties

    private val measuredWidth: Int get() = view.measuredWidth
    private val measuredHeight: Int get() = view.measuredHeight
    private val width: Int get() = view.width
    private val height: Int get() = view.height
    private val isDisplayPortrait get() = view.isDisplayPortrait

    /** The provider of the camera for the process this module is used in (the app) */
    private var cameraProvider: ProcessCameraProvider? = null

    /** The lifecycle this module if bind to */
    private var currentLifecycle: LifecycleOwner? = null

    /** The lifecycle too bind to */
    private var pendingLifecycle: LifecycleOwner? = null

    /** use case builder for preview */
    private val previewBuilder by lazy {
        Preview.Builder().setTargetName(PREVIEW_NAME)
    }

    /** use case builder for capture */
    private val captureBuilder by lazy {
        ImageCapture.Builder().setTargetName(CAPTURE_NAME)
    }

    /** use case for preview */
    private var preview: Preview? = null

    /** use case for capture */
    private var capture: ImageCapture? = null

    /** use case for image analysis */
    private var analysis: ImageAnalysis? = null

    /** camera instance bound to this module */
    private var camera: Camera? = null

    /** Capture Executor */
    private lateinit var captureExecutor: ExecutorService

    /** Image Analysis Executor */
    private lateinit var analysisExecutor: ExecutorService

    /**
     * This values keep track of when the settings of the camera are
     * tried to be changed before even the camera finish being configured.
     */
    private var prematureZoom: Float? = null
    private var prematureTorch: Boolean? = null

    // Internal manipulators
    @LensFacing
    var facing = CameraFacing.BACK
        set(value) {
            if (field == value) return // no-op
            // If we're not bound to a lifecycle, let's keep the facing
            // for the next bind
            field = value
            @Suppress("MissingPermission") currentLifecycle
                ?.let {
                    bindToLifecycle(it)
                }
        }

    var zoom: Float
        get() = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0.0f
        set(@FloatRange(from = 0.0, to = 1.0) value) {
            try {
                camera!!.cameraControl.setLinearZoom(value.coerceIn(0.0f, 1.0f))
            } catch (ex: Exception) {
                prematureZoom = value
            }
        }

    var isTorchEnabled: Boolean
        get() = camera?.cameraInfo?.torchState?.value == TorchState.ON
        set(value) {
            try {
                camera!!.cameraControl.enableTorch(value)
            } catch (ex: Exception) {
                prematureTorch = value
            }
        }

    // Listeners

    private val currentLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(lifecycle: LifecycleOwner) {
            if (lifecycle == currentLifecycle) {
                turnDownExecutors()
                cleanCurrentLifecycle()
                preview?.setSurfaceProvider(null)
            }
        }
    }

    /**
     * Bind the use cases to the cameraX module
     */
    @MainThread
    @RequiresPermission(permission.CAMERA)
    fun bindToLifecycle(lifecycle: LifecycleOwner) {
        if (!view.hasPermissions) {
            Log.w(TAG, "No camera permission granted")
            return
        }
        // we start the executors
        turnUpExecutors()

        pendingLifecycle = lifecycle

        if (measuredWidth > 0 && measuredHeight > 0) {
            bindToLifecycleAfterViewMeasured()
        }
    }

    /**
     * Unbind the use cases from the lifecycle and release all related resources
     */
    @MainThread
    fun unbindFromLifecycle(lifecycle: LifecycleOwner) {
        cameraProvider?.unbindAll()

        // turn down executors.
        turnDownExecutors()
    }

    /**
     * Bind to the lifecycle after the view has been measured and now is displayed
     */
    @MainThread
    @RequiresPermission(permission.CAMERA)
    fun bindToLifecycleAfterViewMeasured() {
        if (!view.hasPermissions) {
            Log.w(TAG, "No camera permission granted")
            return
        }

        pendingLifecycle ?: return

        // we clean the previous use cases and consume the pending lifecycle
        // so when the camera preview is ready, it does find a valid current
        // lifecycle to bind to
        cleanCurrentLifecycle().also {
            currentLifecycle = pendingLifecycle
            pendingLifecycle = null
        }

        if (currentLifecycle?.lifecycle?.currentState == Lifecycle.State.DESTROYED) {
            currentLifecycle = null
            throw IllegalArgumentException("Cannot bind to a lifecycle in a destroyed state")
        }
        cameraProvider ?: return // let's try later when the provider is not null

        // adjust preview resolution based on measured size and aspect ratio
        // we set the appropriate preview size. Do notice that this is only a hint
        // to cameraX, at the end it wil pick a size from the available one that is greater
        // than the one we request but also preserving the aspect ratio.
        // this is the reason we don't use aspect ratio here. We don't want cameraX
        // to chose a big size.
        val previewSize = computePreviewSize().also {
            Log.w(TAG, "Using preview size=$it")
        }

        capture = captureBuilder.apply {
            setTargetRotation(view.displaySurfaceRotation)
            setTargetResolution(previewSize)
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        }.build()


        preview = previewBuilder.apply {
            setTargetResolution(previewSize)
        }.build().apply {
            setSurfaceProvider(view.previewView.createSurfaceProvider())
        }

        // we will use half of the preview size for analysis. we seek for fast analysis here
        if (null != view.analyzer) {
            val analysisOutSize = view.analyzer!!.desiredOutputImageSize(
                view.displayRotationDegrees, isDisplayPortrait, previewSize, ASPECT_RATIO_4_3
            )

            Log.i(TAG, "Installing preview analyzer with image output of size=$analysisOutSize")

            analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(view.displaySurfaceRotation)
                .setTargetResolution(analysisOutSize)
                .setTargetName(ANALYSIS_NAME)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor, ForwardAnalyzer(view.analyzer!!))
                }
        }

        val selector = CameraSelector
            .Builder()
            .requireLensFacing(facing.toFacingConstant)
            .build()

        val useCases = mutableListOf(preview, capture).apply {
            analysis?.let { add(it) }
        }.toTypedArray()

        camera = cameraProvider!!.bindToLifecycle(currentLifecycle!!, selector, *useCases)
            .apply(this::updatePrematureSettings)

        currentLifecycle!!.lifecycle.addObserver(currentLifecycleObserver)
    }

    /**
     * Unbind all the bounded use cases and release the bounded to lifecycle
     */
    @MainThread
    fun cleanCurrentLifecycle() {
        if (!view.hasPermissions) {
            Log.w(TAG, "No camera permission granted")
            return
        }
        cameraProvider?.unbindAll()
        camera = null
        currentLifecycle = null
    }

    /**
     * Compute the preview size used to display the camera preview. The output size respect
     * device orientation and the app aspect ratio, which by this time is 4:3.
     *
     * The preview size is calculated using the host view dimensions as base.
     *
     * e.g If host view dimension is WxH and the view is in portrait then the preview
     * is calculated by making `H` as high as we can get an aspect ratio of H/W
     * because the camera preview will run in landscape and we need to make the H of the
     * preview size as large as we get H/W = aspect ratio because the preview will see H
     * as it W.
     *
     * If on the other side we're in landscape then both, host view and preview will be aligned
     * and then we do nothing.
     *
     * Here's the reasoning, let's suppose our aspect ratio is a/b = 4:3:
     *
     * in portrait mode: we want W/H = b/a and H = W/(b/a) because the preview will see H as
     * width and doing this we actually guarantee that the preview see that H/W = a/b
     *
     * in landscape mode: we know that the device width or view is higher. We want that
     * W/H = a/b and H = W/(a/b) because in landscape still the preview will see that W/H = a/b.
     */
    fun computePreviewSize(): Size {
        // we set the preferred aspect ratio to 4:3
        val targetAspectRatio =
            if (isDisplayPortrait) ASPECT_RATIO_4_3.inverse else ASPECT_RATIO_4_3

        val height = (measuredWidth.toDouble() / targetAspectRatio.toDouble()).toInt()

        return Size(measuredWidth, height)
    }

    fun invalidateView() = updateViewInfo()

    // update view related information used by the use cases
    private fun updateViewInfo() {
        if (!view.hasPermissions) {
            Log.w(TAG, "No camera permission granted")
            return
        }
        capture?.let {
            it.setCropAspectRatio(Rational(width, height))
            it.targetRotation = view.displaySurfaceRotation
        }
        analysis?.let {
            it.targetRotation = view.displaySurfaceRotation
        }
    }

    private fun updatePrematureSettings(camera: Camera) {
        // set them all
        prematureZoom?.let(this::zoom::set)
        prematureTorch?.let(this::isTorchEnabled::set)
        // clean them all
        prematureZoom = null
        prematureTorch = null
    }

    // take proper care of executors lifecycle
    private fun turnUpExecutors() {
        if (::captureExecutor.isInitialized && !captureExecutor.isShutdown) {
            return // this suffix to not initialize more than once the executors
        }
        captureExecutor = Executors.newSingleThreadExecutor()
        analysisExecutor = Executors.newSingleThreadExecutor()
    }

    // take proper care of executors lifecycle
    fun turnDownExecutors() {
        if (!::captureExecutor.isInitialized) {
            return
        }
        captureExecutor.shutdownNow()
        analysisExecutor.shutdownNow()
    }

    fun toggleCamera() {
        facing = if (CameraFacing.BACK == facing)
            CameraFacing.FRONT
        else
            CameraFacing.BACK
    }

    /**
     * Take and save a picture. To guarantee the right display of the saved image, we override
     * [OutputFileOptions.isReversedHorizontal] property.
     *
     * The saved image would be in JPEG format.
     */
    fun takePicture(options: OutputFileOptions, onSaved: OnImageSaved) {
        capture?.internalTakePicture(
            view.context, captureExecutor, options.copy(
                isReversedHorizontal = facing == CameraFacing.FRONT
            ), onSaved
        )
    }

    /**
     * Take a picture and hand it to the caller. The caller must be responsible for closing
     * the associated image.
     */
    fun takePicture(onCaptured: OnImageCaptured) {
        capture?.internalTakePicture(captureExecutor, onCaptured)
    }

    /**
     * [ImageAnalysis.Analyzer] that forward call to out [ImageAnalyzer] interface
     */
    internal class ForwardAnalyzer(private val analyzer: ImageAnalyzer) : ImageAnalysis.Analyzer {
        @UseExperimental(markerClass = ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            analyzer.analyze(ImageProxyImpl(image.image, image.imageInfo.rotationDegrees) {
                image.close()
                true // indicate that we've closed properly the resource
            })
        }
    }

    companion object {
        private const val TAG = "CameraSource"

        // use case names
        private const val PREVIEW_NAME = "Preview"
        private const val CAPTURE_NAME = "Capture"
        private const val ANALYSIS_NAME = "Analysis"

        // aspect ratios
        private val ASPECT_RATIO_4_3 = Rational(4, 3)

        private val Rational.inverse: Rational get() = Rational(denominator, numerator)
    }
}