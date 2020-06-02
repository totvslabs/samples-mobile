package com.totvs.camera.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.core.graphics.toRectF
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.barcode.NullBarcodeObject
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Graphic that display a bounding box for a detected barcode
 */
class BarcodeBoundingBox(
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

    private var barcodeObject: BarcodeObject = NullBarcodeObject

    init {
        setBoundingBoxColors()
    }


    private fun setBoundingBoxColors() {
        strokePaint.apply {
            color = Color.RED
//            alpha = 155
        }
        fillPaint.color = Color.TRANSPARENT
    }

    @MainThread
    override fun onDraw(canvas: Canvas) {
        // [NullBarcodeObject] has null bounding box
        barcodeObject.boundingBox?.toRectF()?.let { box ->
            canvas.drawRoundRect(box, radius, radius, strokePaint)
            canvas.drawRoundRect(box, radius, radius, fillPaint)
        }
    }

    override fun send(value: BarcodeObject) {
        barcodeObject = value

        val padding = this.padding.toInt()
        barcodeObject.boundingBox?.let {
            it.left -= padding
            it.right += padding
            it.top -= padding
            it.bottom += padding
        }
        postInvalidate()
    }
}