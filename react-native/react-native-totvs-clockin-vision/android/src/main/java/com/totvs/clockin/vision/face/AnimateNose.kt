package com.totvs.clockin.vision.face

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import com.totvs.camera.vision.face.*
import com.totvs.camera.vision.stream.Transformer
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Animate [FaceObject] nose
 */
class AnimateNose(
    private val lostObjectDurationMs: Long = 100,
    private val animationDurationMs: Long = 175
) : Transformer<FaceObject, FaceObject> {

    private val handler = Handler(Looper.getMainLooper())

    private var animator: Animator? = null
    private var lastNosePoints: FloatArray? = null

    // we use this only as requirement for the animation
    private object AnimationTarget {
        var nosePosition: FloatArray? = null
    }

    override fun transform(value: FaceObject, receiver: VisionReceiver<FaceObject>) {
        // we lost track of the object we need to schedule to clean the base boundingBox so
        // don't consider it for animations later.
        val nose = value[Nose]

        // if either the face or nose is null, we remove the tracking
        if (value.isNull || nose.isNull) {
            if (null != lastNosePoints) {
                handler.postDelayed(this::clearTracking, lostObjectDurationMs)
            }
            return receiver.send(value)
        }
        // we got back the tracked object, let's remove the callback.
        handler.removeCallbacks(this::clearTracking)

        val points = nosePoints(nose!!)
        val start = lastNosePoints ?: points

        lastNosePoints = points
        animator?.cancel()

        animator = ObjectAnimator
            .ofMultiFloat(AnimationTarget, "nosePosition", arrayOf(start, points))
            .apply {
                duration = animationDurationMs
                interpolator = LinearInterpolator()
                addUpdateListener {
                    lastNosePoints = it.animatedValue as FloatArray

                    receiver.send(value.copy().apply {
                        this[Nose] = lastNosePoints!!.toNose()
                    })
                }
                start()
            }
    }

    private fun clearTracking() {
        lastNosePoints = null
    }

    // Utility functions
    private fun FloatArray.toNose() = Nose(position = PointF(this[0], this[1]))

    private fun nosePoints(nose: Nose) = arrayOf(
        nose.position.x, nose.position.y
    ).toFloatArray()
}