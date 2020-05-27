package com.totvs.camera

import androidx.annotation.AnyThread
import androidx.annotation.FloatRange
import androidx.camera.core.CameraSelector
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIManagerModule
import com.totvs.camera.utils.Constants
import com.totvs.camera.utils.ExportableConstants


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
public class ReactCameraModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    public override fun getName() = NAME

    /**
     * Constants exported to JS side
     */
    public override fun getConstants(): MutableMap<String, Any> {
        return mutableMapOf<String, Any>().apply {
            ExportableConstants.forEach { set ->
                put(set.name, set.export())
            }
        }
    }

    /**
     * Utility function to reduce boilerplate code at executing a block of code
     * on the ui manager
     */
    private fun <T> Promise.withCamera(viewTag: Int, block: CameraView.() -> T) {
        reactApplicationContext.uiManager {
            addUIBlock { manager ->
                try {
                    resolve(manager.cameraViewOrThrow(viewTag).block())
                } catch (ex: Exception) {
                    reject(ex)
                }
            }
        }
    }

    // START View methods

    /**
     * Set camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom
     */
    @AnyThread
    @ReactMethod
    public fun setZoom(
        @FloatRange(from = 0.0, to = 1.0) zoom: Float,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.zoom = zoom
        true
    }

    /**
     * Get camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom
     */
    @AnyThread
    @ReactMethod
    public fun getZoom(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { zoom }

    /**
     * Enable or disable camera torch
     */
    @AnyThread
    @ReactMethod
    public fun enableTorch(
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
    public fun isTorchEnabled(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { isTorchEnabled }

    /**
     * Toggle the camera. i.e if the current camera is the front one it will toggle to back
     * camera as vice versa.
     */
    @AnyThread
    @ReactMethod
    public fun toggleCamera(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) {
            toggleCamera()
            true
        }

    /**
     * Change the camera lens display. This is related to [toggleCamera] in the sence
     * that this method indicate explicitly which lens to use for the camera.
     */
    @AnyThread
    @ReactMethod
    public fun setLensFacing(
        @CameraSelector.LensFacing facing: Int,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.facing = if (Constants.CAMERA_FACING_BACK == facing)
            LensFacing.BACK
        else
            LensFacing.FRONT
    }

    /**
     * Get current camera facing
     */
    @AnyThread
    @ReactMethod
    public fun getLensFacing(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) {
            if (facing == LensFacing.FRONT)
                Constants.CAMERA_FACING_FRONT
            else
                Constants.CAMERA_FACING_BACK
        }


    // experimental api
    @AnyThread
    @ReactMethod
    public fun takePicture(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) {
            takePicture { file -> }
            "Your photo has been saved, check logcat for the path"
        }

    // END View methods

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
         * Extension method on [NativeViewHierarchyManager] to get the [CameraView] or
         * throw if no view can be get for the specified viewTag
         */
        private fun NativeViewHierarchyManager.cameraViewOrThrow(viewTag: Int) =
            checkNotNull(resolveView(viewTag) as? CameraView) {
                "Not possible to resolve CameraView($viewTag)"
            }
    }
}