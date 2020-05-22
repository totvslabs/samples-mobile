package com.totvs.camera

import android.Manifest.permission
import android.util.Rational
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent


/**
 * CameraX use case ope
 */
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

    private val measuredWidth:  Int get() = view.measuredWidth
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

    // Internal manipulators
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
        get() = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1.0f
        set(value) {
            camera!!.cameraControl.setLinearZoom(value.coerceIn(0.0f, 1.0f))
        }

    var isTorchEnabled: Boolean
        get() = camera?.cameraInfo?.torchState?.value == TorchState.ON
        set(value) {
            camera?.cameraControl?.enableTorch(value)
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
    @RequiresPermission(permission.CAMERA)
    fun bindToLifecycle(lifecycle: LifecycleOwner) {
        pendingLifecycle = lifecycle

        if (measuredWidth > 0 && measuredHeight > 0) {
            bindToLifecycleAfterViewMeasured()
        }
    }

    /**
     * Bind to the lifecycle after the view has been measured and now is displayed
     */
    @RequiresPermission(permission.CAMERA)
    fun bindToLifecycleAfterViewMeasured() {
        pendingLifecycle ?: return
        cameraProvider ?: return

        cleanCurrentLifecycle().also {
            currentLifecycle = pendingLifecycle
            pendingLifecycle = null
        }

        if (currentLifecycle?.lifecycle?.currentState == Lifecycle.State.DESTROYED) {
            currentLifecycle = null
            throw IllegalArgumentException("Cannot bind to a lifecycle in a destroyed state")
        }

        val isDisplayPortrait =
            view.displayRotationDegrees == 0 || view.displayRotationDegrees == 180

        // we set the preferred aspect ratio to 4:3
        // @TODO consider using here an aspect ratio computed from display metrics and use the same
        //      aspect ratio for both, preview and capture

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
        currentLifecycle!!.lifecycle.addObserver(currentLifecycleObserver)
    }

    /**
     * Unbind all the bounded use cases and release the bounded to lifecycle
     */
    fun cleanCurrentLifecycle() {
        val toUnbind = with(mutableListOf<UseCase>()) {
            preview?.let { add(it) }
            capture?.let { add(it) }
            toTypedArray()
        }
        cameraProvider?.unbind(*toUnbind)
        camera = null
        currentLifecycle = null
    }

    fun toggleCamera() {
        facing = if (CameraSelector.LENS_FACING_BACK == facing)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }

    fun getRelativeCameraOrientation(compensateForMirroring: Boolean) = camera?.let {
        val degrees = it.cameraInfo
            .getSensorRotationDegrees(view.displaySurfaceRotation)
        if (compensateForMirroring) (360 - degrees) % 360 else degrees
    } ?: 0

    fun invalidateViews() = updateViewInfo()

    // update view related information used by the use cases
    private fun updateViewInfo() {
        capture?.let {
            it.setCropAspectRatio(Rational(width, height))
            it.targetRotation = view.displaySurfaceRotation
        }
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