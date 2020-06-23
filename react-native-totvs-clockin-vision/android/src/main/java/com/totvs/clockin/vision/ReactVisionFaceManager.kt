package com.totvs.clockin.vision

import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.totvs.clockin.vision.annotations.LivenessMode
import com.totvs.clockin.vision.core.LivenessModes
import com.totvs.clockin.vision.core.Model
import com.totvs.clockin.vision.core.ModelProvider
import com.totvs.clockin.vision.events.Event
import com.totvs.clockin.vision.events.OnBarcodeDetected
import com.totvs.clockin.vision.events.OnFaceProximity
import com.totvs.clockin.vision.events.OnLiveness
import com.totvs.clockin.vision.face.LivenessEyes
import com.totvs.clockin.vision.face.LivenessFace
import com.totvs.clockin.vision.face.ProximityByFaceWidth
import com.totvs.clockin.vision.utils.getModelOutputDir
import com.totvs.clockin.vision.view.VisionFaceCameraView

/**
 * [VisionFaceCameraView] react view manager
 */
class ReactVisionFaceManager : AbstractViewManager<VisionFaceCameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName() = VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    override fun createViewInstance(context: ThemedReactContext): VisionFaceCameraView =
        VisionFaceCameraView(context).apply {
            setup(
                ModelProvider.getFaceRecognitionModel(
                    Model.Config(modelDirectory = getModelOutputDir())
                )
            )
        }

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

    // START Setters methods
    /**
     * Sets the appropriate liveness mode on the [cameraView]
     */
    @ReactProp(name = "livenessMode", defaultInt = LivenessModes.NONE)
    fun setLivenessMode(cameraView: VisionFaceCameraView, @LivenessMode mode: Int) {
        cameraView.liveness = when (mode) {
            LivenessModes.EYES -> LivenessEyes {
                // on detection send the event
                OnLiveness(it.mode)(cameraView.context as ReactContext, cameraView.id)
            }.apply { requiredBlinks = TransientState.requiredBlinks }
            LivenessModes.FACE -> LivenessFace {
                // on detection send the event
                OnLiveness(it.mode)(cameraView.context as ReactContext, cameraView.id)
            }
            else -> null // disable the liveness
        }
    }

    /**
     * This property is only related to liveness eyes and control the number of blinks
     * to track until regarding a face as live.
     */
    @ReactProp(name = "blinksCount")
    fun setBlinksCount(cameraView: VisionFaceCameraView, count: Int) {
        TransientState.requiredBlinks = count

        (cameraView.liveness as? LivenessEyes)?.let {
            it.requiredBlinks = count
        }
    }

    /**
     * Set the proximity detector on the [cameraView]
     */
    @ReactProp(name = "isProximityEnabled")
    fun isProximityEnabled(cameraView: VisionFaceCameraView, enabled: Boolean) {
        if (!enabled) {
            cameraView.proximity = null
        } else {
            val proximity = cameraView.proximity as? ProximityByFaceWidth

            if (null != proximity) {
                proximity.threshold = TransientState.proximityThreshold
            } else {
                cameraView.proximity = ProximityByFaceWidth { result ->
                    // on detection send the event,
                    OnFaceProximity(
                        isUnderThreshold = result.isUnderThreshold,
                        threshold = result.threshold,
                        faceWidth = result.faceWidth,
                        faceHeight = result.faceHeight
                    )(cameraView.context as ReactContext, cameraView.id)

                }.apply { threshold = TransientState.proximityThreshold }
            }
        }
    }

    /**
     * Sets the appropriate [cameraView] proximity threshold value
     */
    @ReactProp(name = "proximityThreshold")
    fun setProximityThreshold(cameraView: VisionFaceCameraView, threshold: Float) {
        TransientState.proximityThreshold = threshold

        (cameraView.proximity as? ProximityByFaceWidth)?.let {
            it.threshold = threshold
        }
    }


    // START End methods

    /**
     * With [TransientState] we seek to keep the transient configuration of this vision camera
     * attached to the module instance.
     *
     * We keep here configurations that our managed view doesn't support because are unnatural
     * to it.
     */
    private object TransientState {
        // required blinks for the liveness eye mode.
        var requiredBlinks: Int = 0

        // threshold for the proximity feature.
        var proximityThreshold: Float = 0.0f
    }

    companion object {
        /**
         * Name exported to react native. this will work as the component name. by convention
         * we name the manager with the same name as the view it manages.
         */
        private const val VIEW_NAME = "VisionFaceCameraView"
    }
}