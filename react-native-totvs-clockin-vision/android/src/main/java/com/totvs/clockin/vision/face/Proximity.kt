package com.totvs.clockin.vision.face

import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.VisionReceiver
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Proximity Detector
 */
interface Proximity : VisionReceiver<FaceObject>

/**
 * Proximity Detector that act on [FaceObject] width dimensions.
 */
class ProximityByFaceWidth(
    private val onProximity: (ProximityResult) -> Unit
) : Proximity {
    /**
     * Value that limit the face width to determine when the face width is right, hence
     * regarded as close.
     */
    var threshold: Float = 100f
        set(value) = synchronized(this) {
            field = value
        }
        get() = synchronized(this) {
            field
        }

    /**
     * Control whether we've found a face or not.
     *
     * We use this for situations when the last event we emitted was a
     * valid event for either a right or wrong face and then no face is found.
     * We want to clean that previous state by emitting one event indicating that we're
     * not matching (finding) faces anymore. This allows us to send only one event of this type
     * since it might be the one with the most occurrence.
     *
     * We start with the assumption that we have found a face and let's the
     * detection disprove that assumption
     */
    private val foundFace = AtomicBoolean(true)

    override fun send(value: FaceObject) {
        if (NullFaceObject == value) {
            if (foundFace.compareAndSet(true, false)) {
                onProximity(ProximityResult(
                    threshold = threshold,
                    faceWidth = 0f,
                    faceHeight = 0f
                ))
            }
        } else {
            foundFace.set(true)
            // send a valid event.
            onProximity(ProximityResult(
                isUnderThreshold = value.width <= threshold,
                threshold = threshold,
                faceWidth = value.width,
                faceHeight = value.height
            ))
        }
    }

    override fun toString(): String = TAG

    /**
     * Data representing a proximity detection.
     */
    data class ProximityResult(
        val threshold: Float,
        val faceWidth: Float,
        val faceHeight: Float,
        val isUnderThreshold: Boolean = false
    )

    companion object {
        private const val TAG = "ProximityByFaceWidth"
    }
}