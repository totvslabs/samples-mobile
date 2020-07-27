package com.totvs.clockin.vision

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIManagerModule
import com.totvs.camera.core.Camera
import com.totvs.camera.view.CameraView

/**
 * Abstract class to abstract common operations between [CameraView] subclasses
 */
abstract class AbstractVisionModule(
    context: ReactApplicationContext
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