package com.totvs.camera.view

import android.Manifest.permission
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.annotation.FloatRange
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.totvs.camera.annotations.CameraFacing
import com.totvs.camera.core.OnImageCaptured
import com.totvs.camera.core.OnImageSaved
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.testPictureTake
import java.util.concurrent.Executors


/**
 * CameraX use case ope
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CameraXModule(private val view: CameraView) {

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

    /** camera instance bound to this module */
    private var camera: Camera? = null

    /** Capture Executor */
    private val captureExecutor = Executors.newSingleThreadExecutor()

    /**
     * This values keep track of when the settings of the camera are
     * tried to be changed before even the camera finish being configured.
     */
    private var prematureZoom: Float? = null
    private var prematureTorch: Boolean? = null

    // Internal manipulators
    @CameraFacing
    var facing: Int = CameraSelector.LENS_FACING_BACK
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
            Log.e(TAG, "No camera permission granted")
            return
        }

        pendingLifecycle = lifecycle

        if (measuredWidth > 0 && measuredHeight > 0) {
            bindToLifecycleAfterViewMeasured()
        }
    }

    /**
     * Bind to the lifecycle after the view has been measured and now is displayed
     */
    @MainThread
    @RequiresPermission(permission.CAMERA)
    fun bindToLifecycleAfterViewMeasured() {
        if (!view.hasPermissions) {
            Log.e(TAG, "No camera permission granted")
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

        val isDisplayPortrait =
            view.displayRotationDegrees == 0 || view.displayRotationDegrees == 180

        // we set the preferred aspect ratio to 4:3
        val targetAspectRatio =
            if (isDisplayPortrait) ASPECT_RATIO_4_3.inverse else ASPECT_RATIO_4_3

        capture = captureBuilder.apply {
            setTargetRotation(view.displaySurfaceRotation)
            setTargetAspectRatio(AspectRatio.RATIO_4_3)
        }.build()

        // adjust preview resolution based on measured size and aspect ratio
        val height = (measuredWidth.toDouble() / targetAspectRatio.toDouble()).toInt()
        preview = previewBuilder.apply {
            setTargetResolution(Size(measuredWidth, height))
        }.build().apply {
            setSurfaceProvider(view.previewView.createSurfaceProvider(null))
        }

        val selector = CameraSelector
            .Builder()
            .requireLensFacing(facing)
            .build()

        camera = cameraProvider!!.bindToLifecycle(currentLifecycle!!, selector, preview, capture)
            .apply(this::updatePrematureSettings)

        currentLifecycle!!.lifecycle.addObserver(currentLifecycleObserver)
    }

    /**
     * Unbind all the bounded use cases and release the bounded to lifecycle
     */
    @MainThread
    fun cleanCurrentLifecycle() {
        if (!view.hasPermissions) {
            Log.e(TAG, "No camera permission granted")
            return
        }
        cameraProvider?.unbindAll()
        camera = null
        currentLifecycle = null
    }

    fun invalidateView() = updateViewInfo()

    // update view related information used by the use cases
    private fun updateViewInfo() {
        if (!view.hasPermissions) {
            Log.e(TAG, "No camera permission granted")
            return
        }

        capture?.let {
            it.setCropAspectRatio(Rational(width, height))
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

    fun toggleCamera() {
        facing = if (CameraSelector.LENS_FACING_BACK == facing)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }

    fun takePicture(options: OutputFileOptions, onSaved: OnImageSaved) {
        capture?.testPictureTake(view.context, captureExecutor, facing, options, onSaved)
    }

    fun takePicture(onCaptured: OnImageCaptured) {
        capture?.testPictureTake(captureExecutor, onCaptured)
    }

    companion object {
        private const val TAG = "CameraXModule(totvs)"

        // use case names
        private const val PREVIEW_NAME = "Preview"
        private const val CAPTURE_NAME = "Capture"

        // aspect ratios
        private val ASPECT_RATIO_16_9 = Rational(16, 9)
        private val ASPECT_RATIO_4_3 = Rational(4, 3)

        private val Rational.inverse: Rational get() = Rational(denominator, numerator)
    }
}