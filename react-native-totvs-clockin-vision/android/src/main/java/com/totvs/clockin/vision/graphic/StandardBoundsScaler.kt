package com.totvs.clockin.vision.graphic

import android.graphics.RectF
import android.util.Size
import androidx.annotation.CallSuper
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.stream.Transformer
import com.totvs.camera.vision.stream.VisionReceiver
import kotlin.math.ceil
import kotlin.math.max

/**
 * Generic translate/scale transformer that locate a certain [VisionObject] appropriately
 * into the coordinate system specified by [overlay] dimensions.
 *
 * This scaler uses the same caching strategy of [GraphicOverlay]
 *
 */
abstract class StandardBoundsScaler<T : VisionObject>(
    private val overlay: GraphicOverlay
) : Transformer<T, T> {

    /**
     * Values by which we need to offset scaled values to center then
     * according to this overlay.size center.
     */
    private var offsetX: Int = 0
    private var offsetY: Int = 0

    /**
     * Scale used to scale values into source coordinates to this [overlay] coordinate
     */
    private var scale: Float = 0f

    /**
     * Cached coordinates. optimization purpose
     */
    private var lastSourceCoordinate: Size = Size(0, 0)
    private var lastOverlayCoordinate: Size = Size(0, 0)

    /**
     * Convenience function so that specific implementors clone their appropriate objects.
     */
    protected abstract fun T.clone(
        sourceSize: Size,
        boundingBox: RectF?
    ): T

    @CallSuper
    override fun transform(value: T, receiver: VisionReceiver<T>) {
        // we do nothing when we don't have boundingBox: usually a null object
        val boundingBox = value.boundingBox ?: return receiver.send(value)

        val rotatedSize = when (value.sourceRotationDegrees) {
            0, 180  -> value.sourceSize
            90, 270 -> Size(value.sourceSize.height, value.sourceSize.width)
            else -> throw IllegalArgumentException("Valid rotation are 0, 90, 180, 270")
        }

        ensureScales(rotatedSize)

        // The front facing image is flipped, so we need to mirror the positions on the vertical axis.
        // @TODO fix: this will introduce a bug later,, because this is more like a mirrored flip
        //       rater than just a flip.
        val boundLeft =
            if (overlay.isFrontCamera) rotatedSize.width - boundingBox.right else boundingBox.left
        val boundRight =
            if (overlay.isFrontCamera) rotatedSize.width - boundingBox.left  else boundingBox.right

        val mappedBoundingBox = RectF().apply {
            left   = boundLeft          * scale + offsetX
            right  = boundRight         * scale + offsetX
            top = boundingBox.top * scale + offsetY
            bottom = boundingBox.bottom * scale + offsetY
        }

        receiver.send(value.clone(
            sourceSize = Size(overlay.width, overlay.height),
            boundingBox = mappedBoundingBox
        ))
    }

    /**
     * Scale [x] into this graphic overlay coordinate. Results of this method are only valid
     * after calling [transform], i.e after computing the appropriate scale factors.
     */
    fun scaleX(x: Float): Float {
        return x * scale + offsetX
    }

    /**
     * Scale [y] into this graphic overlay coordinate. Results of this method are only valid
     * after calling [transform], i.e after computing the appropriate scale factors.
     */
    fun scaleY(y: Float): Float {
        return y * scale + offsetY
    }

    /**
     * Translate [x] to this graphic overlay coordinate. Results of this method are only valid
     * after calling [transform], i.e after computing the appropriate scale factors.
     */
    fun translateX(x: Float): Float {
        // The front facing image is flipped, so we need to mirror the positions on the vertical axis
        return if (overlay.isFrontCamera) {
            scaleX(lastSourceCoordinate.width - x)
        } else {
            scaleX(x)
        }
    }

    /**
     * Translate [y] to this graphic overlay coordinate. Results of this method are only valid
     * after calling [transform], i.e after computing the appropriate scale factors.
     */
    protected fun translateY(y: Float): Float = scaleY(y)

    /**
     * This function computes the required values to scale any point in [source]
     * coordinate to [overlay.size] coordinate.
     *
     * Out goals here is to expand the sourceSize as much as it fills the target area but
     * preserving the aspect ratio of the source size. In either orientation in order to
     * expand source size to cover at least the maximum side or target we need to get
     * the max ratio between targetWidth/sourceWidth or targetHeight/sourceHeight
     * because doing so we guarantee that the expanded source size will cover the maximum
     * dimension  of target and will go over the smallest dimension of target. If we chose
     * the smallest ratio instead we might get that the expanded source size will be inside
     * the target area and we don't want that. we want to go over target area but preserving
     * source size aspect ratio. once we determine the ratio multiplying both sides of the
     * source size by the same ratio won't affect it aspect ratio. we use also this ratio
     * to expand the coordinates of the boundingBox.
     */
    private fun ensureScales(source: Size) {
        if (!needsReComputation(source)) return

        val targetWidth = overlay.size.width
        val targetHeight = overlay.size.height

        scale = max(
            targetWidth / source.width.toFloat(),
            targetHeight / source.height.toFloat()
        )
        // this is the new area in which we're gonna display our object.
        val scaledSize = Size(
            ceil(source.width * scale).toInt(),
            ceil(source.height * scale).toInt()
        )

        // calculate the offset to center [scaledSize] into target box.
        offsetX = (targetWidth - scaledSize.width) / 2
        offsetY = (targetHeight - scaledSize.height) / 2

        lastOverlayCoordinate = overlay.size
        lastSourceCoordinate = source
    }

    private fun needsReComputation(source: Size): Boolean =
        source != lastSourceCoordinate || lastOverlayCoordinate != overlay.size
}