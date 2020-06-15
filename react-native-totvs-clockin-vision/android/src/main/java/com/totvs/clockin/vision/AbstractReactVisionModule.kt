package com.totvs.clockin.vision

import androidx.annotation.AnyThread
import androidx.annotation.FloatRange
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIManagerModule
import com.totvs.camera.core.Camera
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.core.CameraFacingConstants
import com.totvs.camera.view.core.CameraZoomLimits
import com.totvs.camera.view.toCameraFacing
import com.totvs.camera.view.toFacingConstant

/**
 * Abstract class to abstract common operations between [CameraView] subclasses
 */
abstract class AbstractReactVisionModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    /**
     * Utility function to reduce boilerplate code at executing a block of code
     * on the ui manager. If [autoResolve] is set to true, then the promise will be
     * resolved with the value returned from the block, otherwise the caller must
     * resolve the promise.
     */
    protected inline fun <reified C : Camera, R> Promise.withCameraDevice(
        viewTag: Int,
        autoResolve: Boolean = true,
        crossinline block: C.(promise: Promise) -> R
    ) = reactApplicationContext.uiManager {
        addUIBlock { manager ->
            try {
                val result = manager.cameraOrThrow<C>(viewTag).block(this@withCameraDevice)
                if (autoResolve) {
                    resolve(result)
                }
            } catch (ex: Exception) {
                reject(ex)
            }
        }
    }

    /**
     * Convenience method to get a camera instance on a reified parameter.
     */
    private inline fun <R> Promise.withCamera(
        viewTag: Int,
        autoResolve: Boolean = true,
        crossinline block: Camera.(promise: Promise) -> R
    ) = withCameraDevice(viewTag, autoResolve, block)

    // START View methods

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

    // END View methods

    companion object {
        /**
         * Extension method on [ReactApplicationContext] to get the ui manager module
         */
        protected fun ReactApplicationContext.uiManager(block: UIManagerModule.() -> Unit) {
            (getNativeModule(UIManagerModule::class.java) as UIManagerModule).block()
        }

        /**
         * Extension method on [NativeViewHierarchyManager] to get the a camera of type [C] or
         * throw if no view can be get for the specified viewTag
         */
        protected inline fun <reified C : Camera> NativeViewHierarchyManager.cameraOrThrow(
            viewTag: Int
        ): C =
            checkNotNull(resolveView(viewTag) as? C) {
                "Not possible to resolve CameraView($viewTag) as ${C::class.java} type"
            }
    }
}