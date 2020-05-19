package com.totvs.camera.bridge

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.totvs.camera.TOTVSCameraView
import com.totvs.camera.events.Event

/**
 * [TOTVSCameraView] react native manager
 *
 * @author Jansel Valentin
 */
class TOTVSCameraManager : ViewGroupManager<TOTVSCameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName(): String =
        VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    override fun createViewInstance(context: ThemedReactContext): TOTVSCameraView =
        TOTVSCameraView(context)

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
        private const val VIEW_NAME = "TOTVSCameraView"
    }
}