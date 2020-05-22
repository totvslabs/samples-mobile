package com.totvs.camera


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
    override fun getName(): String =
        VIEW_NAME

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

    // START View methods: Set here all the method required to configure the view

    // END View methods:

    companion object {
        /**
         * Name exported to react native. this will work as the component name
         */
        private const val VIEW_NAME = "CameraView"
    }
}