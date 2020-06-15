package com.totvs.clockin.vision

import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.totvs.clockin.vision.events.Event
import com.totvs.clockin.vision.events.OnBarcodeDetected
import com.totvs.clockin.vision.view.FaceVisionCameraView

/**
 * [FaceVisionCameraView] react view manager
 */
class ReactVisionFaceManager : ViewGroupManager<FaceVisionCameraView>() {

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

    // START Setters methods

    // END Setters methods

    companion object {
        /**
         * Name exported to react native. this will work as the component name. by convention
         * we name the manager with the same name as the view it manages.
         */
        private const val VIEW_NAME = "FaceVisionCameraView"
    }
}