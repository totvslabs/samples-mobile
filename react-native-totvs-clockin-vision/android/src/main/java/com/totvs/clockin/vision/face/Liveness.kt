package com.totvs.clockin.vision.face

import com.facebook.react.bridge.ReactContext
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.VisionReceiver
import com.totvs.clockin.vision.events.OnLiveness

/**
 * Liveness detection mode.
 */
sealed class Liveness : VisionReceiver<FaceObject>

/**
 * Detect face movement
 *
 * @param infeasibleAreaPercent controls what horizontal percentage of the screen
 * will be regarded as not feasible region for the face to be to be even considered
 * for liveness. This parameter help us to deal with the situation that one face might
 * be in a corner of the screen coordinate system and we can't recognize it quite well.
 */
class LivenessFace(
    val context: ReactContext,
    val viewId: Int,
    val infeasibleAreaPercent: Float = 0.2f
) : Liveness() {

    override fun send(value: FaceObject) {
        
    }

    override fun toString() = TAG

    companion object {
        /**
         * Id value to be exported as representative of this liveness type
         */
        internal const val id = 1

        private const val TAG = "Liveness(Face)"
    }
}

/**
 * Detect eyes blinking
 */
class LivenessEyes(
    val context: ReactContext,
    val viewId: Int,
    var requiredBlinks: Int = NO_BLINKS
) : Liveness() {
    /**
     * Keep track of the previous probability tracked.
     */
    private var previousEyesOpenProb = 0f

    /**
     * Keep track of the blink count before emitting an event.
     */
    private var blinks = 0

    override fun send(value: FaceObject) {
        // we try not to perform unnecessary work
        if (NO_BLINKS == requiredBlinks || NullFaceObject == value) {
            return
        }
        if (meetRequiredBlinks(value)) {
            OnLiveness(mode = id)(context, viewId)
        }
    }

    private fun meetRequiredBlinks(face: FaceObject) : Boolean {
        // Get the lowest value from both eyes.
        val eyesOpenProb =
            face.eyesOpenProbability.left.coerceAtMost(face.eyesOpenProbability.right)
        // Were the eyes open previously?
        var meetRequirement = false

        if (previousEyesOpenProb > EYES_OPEN_THRESHOLD) {
            // Are the eyes closed?
            if (eyesOpenProb < EYES_CLOSED_THRESHOLD) {
                blinks++

                if (blinks >= requiredBlinks) {
                    meetRequirement = true
                    blinks = 0
                }
            }
        }
        previousEyesOpenProb = eyesOpenProb

        return meetRequirement
    }

    override fun toString() = TAG

    companion object {
        /**
         * Id value to be exported as representative of this liveness type
         */
        internal const val id = 2

        private const val TAG = "Liveness(Eyes)"

        /**
         * Value that defined no required blinks
         */
        private const val NO_BLINKS = -1

        /**
         * Value to define if eyes are closed.
         */
        private const val EYES_CLOSED_THRESHOLD = 0.6f

        /**
         * Value to define if eyes are open.
         */
        private const val EYES_OPEN_THRESHOLD = 0.95f
    }
}