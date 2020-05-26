package com.totvs.camera


import androidx.annotation.AnyThread
import androidx.annotation.FloatRange
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.totvs.camera.events.Event

/**
 * [CameraView] react native manager
 *
 */
class ReactCameraManager : ViewGroupManager<CameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName() = VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    @Suppress("MissingPermission")
    override fun createViewInstance(context: ThemedReactContext): CameraView =
        CameraView(context)

    /**
     * Register events
     */
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        val events = mutableMapOf<String, Any>()
        // Event.forEach { e -> ... } will also work or Event.exported.forEach { e -> .. }
        for (e in Event) {
            events[e.name] = mutableMapOf("registrationName" to e.name)
        }
        return events
    }

    // START View methods

    /**
     * Set camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom
     */
    @AnyThread
    public fun setZoom(@FloatRange(from = 0.0, to = 1.0) zoom: Float, promise: Promise) {
    }

    /**
     * Enable or disable camera torch
     */
    @AnyThread
    public fun enableTorch(enable: Boolean, promise: Promise) {
    }

    /**
     * Toggle the camera. i.e if the current camera is the front one it will toggle to back
     * camera as vice versa.
     */
    @AnyThread
    public fun toggleCamera(promise: Promise) {

    }

    /**
     * Change the camera lens display. This is related to [toggleCamera] in the sence
     * that this method indicate explicitly which lens to use for the camera.
     */
    @AnyThread
    public fun setLensFacing(promise: Promise) {

    }
    // END View methods

    companion object {
        /**
         * Name exported to react native. this will work as the component name
         */
        private const val VIEW_NAME = "CameraView"
    }
}