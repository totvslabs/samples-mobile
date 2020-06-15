package com.totvs.clockin.vision

import com.facebook.react.uimanager.ThemedReactContext
import com.totvs.clockin.vision.events.Event
import com.totvs.clockin.vision.events.OnBarcodeDetected
import com.totvs.clockin.vision.view.FaceVisionCameraView

/**
 * [FaceVisionCameraView] react view manager
 */
class ReactVisionFaceManager : AbstractViewManager<FaceVisionCameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName() = VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    override fun createViewInstance(context: ThemedReactContext): FaceVisionCameraView =
        FaceVisionCameraView(context)

    /**
     * Register events
     *
     * Modifications on this method are required to filter out events not related to face
     * capability. Nothing will happens if not filter is made but is advised.
     */
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        val events = mutableMapOf<String, Any>()
        for (e in Event) {
            // we here don't regard about barcode events.
            if (e == OnBarcodeDetected) {
                continue
            }
            events[e.name] = mutableMapOf("registrationName" to e.name)
        }
        return events
    }

    companion object {
        /**
         * Name exported to react native. this will work as the component name. by convention
         * we name the manager with the same name as the view it manages.
         */
        private const val VIEW_NAME = "FaceVisionCameraView"
    }
}