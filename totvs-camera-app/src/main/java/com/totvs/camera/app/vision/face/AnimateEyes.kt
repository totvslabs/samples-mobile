package com.totvs.camera.app.vision.face

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.LeftEye
import com.totvs.camera.vision.face.RightEye
import com.totvs.camera.vision.face.isNull
import com.totvs.camera.vision.stream.Transformer
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Animate [FaceObject] eyes
 */
class AnimateEyes(
    private val lostObjectDurationMs: Long = 100,
    private val animationDurationMs: Long = 100
) : Transformer<FaceObject, FaceObject> {

    private val handler = Handler(Looper.getMainLooper())

    private var animator: Animator? = null
    private var lastEyesPoints: FloatArray? = null

    // we use this only as requirement for the animation
    private object AnimationTarget {
        var eyesPosition: FloatArray? = null
    }

    override fun transform(value: FaceObject, receiver: VisionReceiver<FaceObject>) {
        // we lost track of the object we need to schedule to clean the base boundingBox so
        // don't consider it for animations later.
        val lEye = value[LeftEye]
        val rEye = value[RightEye]

        // if either the face of any of the eyes is null, we remove the tracking
        if (value.isNull || lEye.isNull || rEye.isNull) {
            if (null != lastEyesPoints) {
                handler.postDelayed(this::clearTracking, lostObjectDurationMs)
            }
            return receiver.send(value)
        }
        // we got back the tracked object, let's remove the callback.
        handler.removeCallbacks(this::clearTracking)

        val points = eyesPoints(lEye!!, rEye!!)
        val start = lastEyesPoints ?: points

        lastEyesPoints = points
        animator?.cancel()

        animator = ObjectAnimator
            .ofMultiFloat(AnimationTarget, "eyesPosition", arrayOf(start, points))
            .apply {
                duration = animationDurationMs
                interpolator = LinearInterpolator()
                addUpdateListener {
                    lastEyesPoints = it.animatedValue as FloatArray

                    receiver.send(value.copy().apply {
                        this[LeftEye]  = lastEyesPoints!!.leftEye()
                        this[RightEye] = lastEyesPoints!!.rightEye()
                    })
                }
                start()
            }
    }

    private fun clearTracking() {
        lastEyesPoints = null
    }

    // Utility functions
    private fun FloatArray.leftEye()  = LeftEye(position = PointF(this[0], this[1]))
    private fun FloatArray.rightEye() = RightEye(position = PointF(this[2], this[3]))

    private fun eyesPoints(leftEye: LeftEye, rightEye: RightEye) = arrayOf(
        leftEye.position.x, leftEye.position.y,
        rightEye.position.x, rightEye.position.y
    ).toFloatArray()
}