package com.totvs.camera.app.vision

import android.graphics.RectF
import android.util.Log
import android.util.Size
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.stream.Transformer
import com.totvs.camera.vision.stream.VisionReceiver
import kotlin.math.ceil
import kotlin.math.max

/**
 * Generic translate transformer that locate a certain [VisionObject] appropriately
 * into the coordinate system specified by [overlay] dimensions
 */
abstract class TranslateBounds<T : VisionObject>(
    private val overlay: GraphicOverlay
) : Transformer<T, T> {

    /**
     * Convenience function so that specific implementors clone their appropriate objects.
     */
    protected abstract fun T.clone(
        sourceSize: Size,
        boundingBox: RectF?
    ): T

    override fun transform(value: T, receiver: VisionReceiver<T>) {
        // we do nothing when we don't have boundingBox: usually a null object
        val boundingBox = value.boundingBox ?: return receiver.send(value)

        val targetWidth = overlay.width
        val targetHeight = overlay.height

        /*
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
        val rotatedSize = when (value.sourceRotationDegrees) {
            0, 180 -> value.sourceSize
            90, 270 -> Size(value.sourceSize.height, value.sourceSize.width)
            else -> throw IllegalArgumentException("Valid rotation are 0, 90, 180, 270")
        }

        val scale = max(
            targetWidth / rotatedSize.width.toFloat(),
            targetHeight / rotatedSize.height.toFloat()
        )
        // this is the new area in which wr're gonna display our object.
        val scaledSize = Size(
            ceil(rotatedSize.width * scale).toInt(),
            ceil(rotatedSize.height * scale).toInt()
        )

        // calculate the offset to center [scaledSize] into target box.
        val offsetX = (targetWidth - scaledSize.width) / 2
        val offsetY = (targetHeight - scaledSize.height) / 2

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
            sourceSize = Size(targetWidth, targetHeight),
            boundingBox = mappedBoundingBox
        ))
    }
}