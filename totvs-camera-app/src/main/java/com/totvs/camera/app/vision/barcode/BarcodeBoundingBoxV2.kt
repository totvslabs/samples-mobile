package com.totvs.camera.app.vision.barcode

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Size
import androidx.annotation.MainThread
import com.totvs.camera.app.R
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Graphic that display a bounding box for a detected barcode.
 *
 * This version of the boundingBox illustrates how to use the utility functions that comes
 * with [GraphicOverlay.Graphic].
 *
 * A sample use of this graphic would be:
 *
 * analyzer.detections
 *  .filterIsInstance<BarcodeObject>()
 *  .sendOn(ContextCompat.getMainExecutor(this))
 *  .transform(AnimateBarcode()) // on main thread
 *  .connect(barcodeBoundingBox)
 *
 */
class BarcodeBoundingBoxV2(
    context: Context
) : GraphicOverlay.Graphic(), VisionReceiver<BarcodeObject> {

    // paint properties
    private val padding = context.resources.getDimension(R.dimen.barcode_bounding_box_padding)
    private val radius = context.resources.getDimension(R.dimen.barcode_bounding_box_corner_radius)
    private val stroke = context.resources.getDimension(R.dimen.barcode_bounding_box_stroke_width)

    // painters
    private val strokePaint = Paint().apply {
        strokeWidth = stroke
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var boundingBox: RectF? = null

    init {
        setBoundingBoxColors()
    }

    private fun setBoundingBoxColors() {
        strokePaint.apply {
            color = Color.RED
            alpha = 140
        }
        fillPaint.apply {
            color = Color.RED
            alpha = 45
        }
    }

    @MainThread
    override fun onDraw(canvas: Canvas) {
        // [NullBarcodeObject] has null bounding box
        boundingBox?.let { box ->
            canvas.drawRoundRect(box, radius, radius, strokePaint)
            canvas.drawRoundRect(box, radius, radius, fillPaint)
        }
    }

    override fun send(value: BarcodeObject) {
        boundingBox = value.boundingBox

        if (null != boundingBox) {
            // we need to rotate the source size according the rotation
            val rotatedSize = when (value.sourceRotationDegrees) {
                0, 180 -> value.sourceSize
                90, 270 -> Size(value.sourceSize.height, value.sourceSize.width)
                else -> throw IllegalArgumentException("Valid rotation are 0, 90, 180, 270")
            }

            // The front facing image is flipped, so we need to mirror the positions on the vertical axis
            val left  = if (overlay.isFrontCamera) boundingBox!!.right else boundingBox!!.left
            val right = if (overlay.isFrontCamera) boundingBox!!.left  else boundingBox!!.right

            boundingBox?.let {
                it.left   = translateX(left,      rotatedSize) - padding
                it.right  = translateX(right,     rotatedSize) + padding
                it.top    = translateY(it.top,    rotatedSize) - padding
                it.bottom = translateY(it.bottom, rotatedSize) + padding
            }
        }

        postInvalidate()
    }
}