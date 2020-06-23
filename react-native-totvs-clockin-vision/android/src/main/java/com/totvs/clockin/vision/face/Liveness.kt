package com.totvs.clockin.vision.face

import com.facebook.react.bridge.ReactContext
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.Nose
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.VisionReceiver
import com.totvs.clockin.vision.events.OnLiveness
import java.util.*
import javax.annotation.concurrent.GuardedBy

/**
 * Liveness detection mode.
 */
sealed class Liveness : VisionReceiver<FaceObject>

/**
 * Data representing the result of a liveness detection
 */
data class LivenessResult(val mode: Int)

/**
 * Detect face movement
 *
 * @param infeasibleAreaPercent controls what horizontal percentage of the screen
 * will be regarded as not feasible region for the face to be to be even considered
 * for liveness. This parameter help us to deal with the situation that one face might
 * be in a corner of the screen coordinate system and we can't recognize it quite well.
 */
class LivenessFace(
    private val infeasibleAreaPercent: Float = 0.2f,
    private val onLiveness: (LivenessResult) -> Unit
) : Liveness() {

    private val state = YawState()

    override fun send(value: FaceObject) {
        // we only consider faces with noses landmark here
        val nose = value[Nose] ?: return
        val cx = nose.position.x // already translated by [FaceNoseTranslator]
        // y-euler face rotation
        val eulerY = value.eulerY

        val boundary = value.sourceSize.width * infeasibleAreaPercent
        // If the nose landmark x coordinate is within bounds proceed.
        if (boundary < cx && cx < (value.sourceSize.width - boundary)) {
            state.push(value.eulerY.toInt()) { // if activated

                if (-EULER_ANGLE_CENTER_BOUND < eulerY && eulerY < EULER_ANGLE_CENTER_BOUND) {
                    // is live. clear
                    state.clear()
                    // emit event
                    onLiveness(LivenessResult(mode = id))
                }
            }
        } else {
            state.clear()
        }
    }

    override fun toString() = TAG

    private class YawState {
        @GuardedBy("this")
        private val yaws = TreeSet<Int>()

        @Synchronized
        fun push(yaw: Int, onActivated: () -> Unit) {
            yaws.add(yaw)
            if (isActivated()) {
                onActivated()
            }
        }

        @Synchronized
        private fun isActivated(): Boolean {
            val yaws = this.yaws.toList()
            // Since the array is ordered from negative to positive.
            return if (yaws.isEmpty()) false else {
                -YAW_THRESHOLD >= yaws[0] && yaws.last() >= YAW_THRESHOLD
            }
        }

        @Synchronized
        fun clear() = yaws.clear()
    }

    companion object {
        /**
         * Id value to be exported as representative of this liveness type
         */
        internal const val id = 1

        private const val TAG = "Liveness(Face)"

        // controls the yaw activation function
        private const val YAW_THRESHOLD = 13

        //Bound is used to determine if the users face has returned to the
        // center of the screen.
        private const val EULER_ANGLE_CENTER_BOUND = 5
    }
}

/**
 * Detect eyes blinking
 */
class LivenessEyes(
    private val onLiveness: (LivenessResult) -> Unit
) : Liveness() {

    var requiredBlinks: Int = NO_BLINKS
        get() = synchronized(this) {
            field
        }
        set(value) = synchronized(this) {
            field = value
        }

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
            onLiveness(LivenessResult(mode = id))
        }
    }

    private fun meetRequiredBlinks(face: FaceObject): Boolean {
        // Get the lowest value from both eyes.
        val eyesOpenProb =
            face.eyesOpenProbability.left.coerceAtMost(face.eyesOpenProbability.right)

        if (eyesOpenProb == UNCOMPUTED_PROBABILITY) {
            return false
        }
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
         * non-computed probability
         */
        private const val UNCOMPUTED_PROBABILITY = -1.0f

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