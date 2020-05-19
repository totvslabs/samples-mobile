package com.totvs.camera

import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.totvs.camera.events.Event

/**
 * [CameraView] react native manager
 *
 * @author Jansel Valentin
 */
class CameraManager : ViewGroupManager<CameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName(): String =
        VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    override fun createViewInstance(context: ThemedReactContext): CameraView =
        CameraView(context)

    /**
     * Register events
     */
    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val events = mutableMapOf<String, Any>()
        Event.exported.forEach { export ->
            events[export.name] = mapOf("registrationName" to export.name)
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