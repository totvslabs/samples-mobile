package com.totvs.camera.app.vision

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.MainThread
import com.totvs.camera.app.R
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Graphic that display a bounding box for a detected barcode.
 *
 * This version of the boundingBox illustrates how to pre-process [VisionObject] coordinates
 * before being drawn. This bounding box is supposed to work in conjunction with [TranslateBounds]
 * transformer.
 *
 * Undefined behavior will be noticed if this graphic is not used with [TranslateBounds]
 * because the coordinate system of this graphic and the object it receives wouldn't be
 * the same.
 *
 * A sample use of this graphic would be:
 *
 * analyzer.detections
 *  .filterIsInstance<BarcodeObject>()
 *  .transform(TranslateBarcode(camera.graphicOverlay)) // install the coordinate translate transformer
 *  .sendOn(ContextCompat.getMainExecutor(this))
 *  .transform(AnimateBarcode()) // on main thread
 *  .connect(barcodeBoundingBox)
 *
 */
class BarcodeBoundingBoxV1(
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

        boundingBox?.let {
            it.left   -= padding
            it.right  += padding
            it.top    -= padding
            it.bottom += padding
        }
        postInvalidate()
    }
}