package com.totvs.camera

import androidx.annotation.AnyThread
import androidx.annotation.FloatRange
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIManagerModule
import com.totvs.camera.core.Camera
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.core.CameraFacingConstants
import com.totvs.camera.view.core.ExportableConstant
import com.totvs.camera.view.toCameraFacing
import com.totvs.camera.view.toFacingConstant
import com.totvs.camera.view.core.CameraZoomLimits
import java.io.File

/**
 * React Native module for this library.
 */
/*
 * Please keep in mind that the runtime react-native library used to compile
 * the library is different from the version resolved by gradle when used in a react-native
 * project. The version resolved by gradle which is around 0.20.1 while the one resolved
 * when compiling the library included in react native project depends on the version
 * used when creating the project.
 *
 * It turns out that the 0.20.1 library is not compatible with the runtime found
 * in node_modules. That said, DON'T fix the compile time errors on this file. They're there
 * to reflect the actual signature of the runtime library with which the project is
 * compiled.
 *
 * When you work on this library you can specify on build.gradle the node_modules directory
 * to be used as a source for the react-native library implementation to allow this code
 * to compile.
 *
 * Read documentation in react-native-totvs-camera/build.gradle to know how to handle this
 * situation for compile time.
 *
 * @see also [ReactCameraModule]
 */
class ReactCameraModule(
    context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    override fun getName() = NAME

    /**
     * Constants exported to JS side
     */
    override fun getConstants(): MutableMap<String, Any> {
        return mutableMapOf<String, Any>().apply {
            ExportableConstant.forEach { set ->
                put(set.name, set.export())
            }
        }
    }

    /**
     * Utility function to reduce boilerplate code at executing a block of code
     * on the ui manager. If [autoResolve] is set to true, then the promise will be
     * resolved with the value returned from the block, otherwise the caller must
     * resolve the promise
     */
    private fun <T> Promise.withCamera(
        viewTag: Int,
        autoResolve: Boolean = true,
        block: Camera.(promise: Promise) -> T
    ) = reactApplicationContext.uiManager {
        addUIBlock { manager ->
            try {
                val result = manager.cameraOrThrow(viewTag).block(this@withCamera)
                if (autoResolve) {
                    resolve(result)
                }
            } catch (ex: Exception) {
                reject(ex)
            }
        }
    }

    // START Module methods

    /**
     * Set camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @AnyThread
    @ReactMethod
    fun setZoom(
        @FloatRange(from = 0.0, to = 1.0) zoom: Float,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.zoom = zoom
        true
    }

    /**
     * Get camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @AnyThread
    @ReactMethod
    fun getZoom(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { zoom }

    /**
     * Enable or disable camera torch
     */
    @AnyThread
    @ReactMethod
    fun enableTorch(
        enable: Boolean,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        isTorchEnabled = enable
        true
    }

    /**
     * whether or not the camera torch is enabled
     */
    @AnyThread
    @ReactMethod
    fun isTorchEnabled(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { isTorchEnabled }

    /**
     * Toggle the camera. i.e if the current camera is the front one it will toggle to back
     * camera as vice versa.
     */
    @AnyThread
    @ReactMethod
    fun toggleCamera(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) {
            toggleCamera()
            true
        }

    /**
     * Change the camera lens display. This is related to [toggleCamera] in the sense
     * that this method indicate explicitly which lens to use for the camera.
     *
     * [facing] is expressed as one of the exported constants [CameraFacingConstants]
     */
    @AnyThread
    @ReactMethod
    fun setLensFacing(
        @LensFacing facing: Int,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.facing = facing.toCameraFacing
        true
    }

    /**
     * Get current camera facing. Returned facing is expected to be one of the exported
     * facing constants [CameraFacingConstants].
     */
    @AnyThread
    @ReactMethod
    fun getLensFacing(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { facing.toFacingConstant }

    /**
     * Even though the [Camera] interface offers two variation of [takePicture]
     * here we only expose what's relevant to a react-native app at this moment. The
     * one that save the image into an specified location.
     *
     * This doesn't restrict from exposing the counter part capture method, we only need
     * to figure out how/what to send up to the app as a representation of the captured
     * image.
     */
    @AnyThread
    @ReactMethod
    public fun takePicture(viewTag: Int, outputDir: String?, promise: Promise) {
        val options = OutputFileOptions(outputDirectory = try {
                File(outputDir!!)
            } catch (ex: Exception) { null }
        )

        promise.withCamera(viewTag, autoResolve = false) {
            takePicture(options = options) { file, throwable ->
                throwable?.let {
                    promise.reject(it)
                }
                file?.let {
                    promise.resolve(it.absolutePath)
                }
            }
            true
        }
    }

    // END Module methods

    companion object {
        /**
         * Exported name of the module representing this library
         */
        private const val NAME = "CameraModule"

        /**
         * Extension method on [ReactApplicationContext] to get the ui manager module
         */
        private fun ReactApplicationContext.uiManager(block: UIManagerModule.() -> Unit) {
            (getNativeModule(UIManagerModule::class.java) as UIManagerModule).block()
        }

        /**
         * Extension method on [NativeViewHierarchyManager] to get the [CameraView] as [Camera] or
         * throw if no view can be get for the specified viewTag
         */
        private fun NativeViewHierarchyManager.cameraOrThrow(viewTag: Int) : Camera =
            checkNotNull(resolveView(viewTag) as? CameraView) {
                "Not possible to resolve CameraView($viewTag)"
            }
    }
}