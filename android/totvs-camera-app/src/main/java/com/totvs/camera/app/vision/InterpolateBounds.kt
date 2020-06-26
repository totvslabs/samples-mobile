package com.totvs.camera.app.vision

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.stream.Transformer
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Transform one [VisionObject] into multiple vision objects that represent
 * smooth interpolation of the received one. This help us to compensate with
 * lost preview frames.
 *
 * @param lostObjectDurationMs duration in milliseconds to wait until stopping to send
 * interpolated objects after receiving a null value.
 * @param animationDurationMs duration of the animation interpolation.
 */
abstract class InterpolateBounds<T : VisionObject>(
    private val lostObjectDurationMs: Long = 200,
    private val animationDurationMs: Long = 100
) : Transformer<T, T> {

    private val handler = Handler(Looper.getMainLooper())

    private var lastBoundingBox: RectF? = null
    private var animator: Animator? = null

    // we use this only as requirement for the animation
    private object AnimationTarget {
        var boundingBox: FloatArray? = null
    }

    /**
     * Convenience function so that specific implementors clone their appropriate objects.
     */
    protected abstract fun T.clone(
        boundingBox: RectF?
    ): T

    override fun transform(value: T, receiver: VisionReceiver<T>) {
        // we lost track of the object we need to schedule to clean the base boundingBox so
        // don't consider it for animations later.
        if (value.boundingBox == null) {
            if (lastBoundingBox != null) {
                handler.postDelayed(this::clearTracking, lostObjectDurationMs)
            }
            return receiver.send(value)

        }
        // we got back the tracked object, let's remove the callback.
        handler.removeCallbacks(this::clearTracking)

        val start = lastBoundingBox?.toEdges() ?: value.boundingBox?.toEdges()
        val end = value.boundingBox?.toEdges()

        lastBoundingBox = value.boundingBox
        animator?.cancel()

        // call "setBoundingBox" for animations
        animator = ObjectAnimator.ofMultiFloat(AnimationTarget, "boundingBox", arrayOf(start, end)).apply {
            duration = animationDurationMs
            interpolator = LinearInterpolator()
            addUpdateListener {
                // The automated call to setBoundingBox does not work...Kotlin?
                lastBoundingBox = (it.animatedValue as FloatArray).toRectF()
                receiver.send(value.clone(
                    boundingBox = lastBoundingBox?.copy()
                ))
            }
            start()
        }
    }

    /**
     * Clear the last tracked bounding box
     */
    private fun clearTracking() {
        lastBoundingBox = null
    }

    private fun RectF.copy() = RectF(left, top, right, bottom)

    private fun RectF.toEdges() = arrayOf(left, top, right, bottom).toFloatArray()

    private fun FloatArray.toRectF() = RectF().also {
        it.left = this[0]
        it.top = this[1]
        it.right = this[2]
        it.bottom = this[3]
    }
}