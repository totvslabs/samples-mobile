package com.totvs.camera.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.totvs.camera.*
import com.totvs.camera.Camera
import com.totvs.camera.ReactLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

typealias CoreCamera = androidx.camera.core.Camera

/**
 * Main view where the camera preview is rendered. It conform the set of camera operations
 * that supported through [Camera]. Other operations invoked in this view apart from the
 * one stated in the [Camera] contract, are not guaranteed to be available in future versions
 * of this view.
 *
 * @author Jansel Valentin
 */
public class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : PreviewView(context, attrs, style), Camera, LifecycleObserver, LifecycleEventListener {

    // [Camera] contract
    override var isFlashEnabled: Boolean
        get() = camera!!.cameraInfo.torchState.value == TorchState.ON
        set(value) {
            camera!!.cameraControl.enableTorch(value)
        }

    private var displayId: Int? = -1
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: CoreCamera? = null


    private val displayManager by lazy {
        context.applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /**
     * Lifecycle owner to control [CameraX] lifecycle.
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
     * when running on a reac-native environment, otherwise throught a subscription
     * to the holder context.
     *
     * @see also [ReactLifecycleOwner]
     */
    private val lifecycleOwner: LifecycleOwner =
        ((context as? LifecycleOwner) ?: ReactLifecycleOwner)
            .also {
                require(context is LifecycleOwner || context is ThemedReactContext) {
                    "Invalid context type. You must use this view with a LifecycleOwner or ThemedReactContext context"
                }

                when (context) {
                    is LifecycleOwner -> context.lifecycle.addObserver(this)
                    is ThemedReactContext -> context.addLifecycleEventListener(this)
                }
            }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * We need a display listener for orientation changes that do not trigger configuration
     * changes, for example if we choose to override config change in manifest or for 180 degree
     * orientation change
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == displayId) {
                imageCapture?.targetRotation = display.rotation
            }
            Unit
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        if (!hasPermissions(context)) return

        cameraExecutor = Executors.newSingleThreadExecutor()

        displayManager.registerDisplayListener(displayListener, null)
        // wait for this view to render properly
        post {
            displayId = display.displayId
            bindCamerasUseCases()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() = Unit


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        if (!::cameraExecutor.isInitialized) return

        cameraExecutor.shutdown()

        displayManager.unregisterDisplayListener(displayListener)
    }


    /** Here we declare and bind capture and preview use cases */
    private fun bindCamerasUseCases() {
        val metrics = DisplayMetrics().also { display.getRealMetrics(it) }

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val futureProvider = ProcessCameraProvider.getInstance(context)
        futureProvider.addListener(Runnable {

            // [CameraProvider]
            val provider = futureProvider.get()

            // [Preview]
            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // [ImageCapture]
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // [ImageAnalysis]
            // @TODO When time comes, don't forget schedule the analyzer on a different
            //       executor than [cameraExecutor] this way you don't interfere with
            //       [imageCapture] use case.

            // must unbind all the previous use cases before binding them again
            provider.unbindAll()
            try {
                camera = provider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
                // Attach the preview surface provider to preview use case
                preview?.setSurfaceProvider(createSurfaceProvider(camera?.cameraInfo))
            } catch (ex: Exception) {
                Log.e(TAG, "Use case binding failed", ex)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height).toDouble()
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    // [Camera] contracts
    override fun takePicture(onTaken: OnPictureTakenCallback) {
        imageCapture!!.testPictureTake(context, cameraExecutor, lensFacing, onTaken)
    }

    override fun setTargetRotation(rotation: Int) {
        // @TODO confirm if this is really necessary after this implementation
    }

    override fun setFacing(facing: LensFacing) {
        if (facing() == lensFacing) {
            return
        }
        lensFacing = facing()
        bindCamerasUseCases()
    }

    override fun zoom(zoom: Float) {
        camera!!.cameraControl.setLinearZoom(zoom.coerceIn(0.0f, 1.0f))
    }

    // React Native [LifecycleEventListener] events
    override fun onHostResume() =
        (ReactLifecycleOwner.onHostResume() ?: Unit).also { onStart() }

    override fun onHostPause() =
        (ReactLifecycleOwner.onHostPause() ?: Unit).also { onStop() }

    override fun onHostDestroy() =
        (ReactLifecycleOwner.onHostDestroy() ?: Unit).also { onDestroy() }

    // Companion & Objects
    companion object {
        private val TAG = CameraView::class.java.simpleName
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
