package com.totvs.camera.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.stream.VisionReceiver

class FaceGraphic(
    context: Context
) : GraphicOverlay.Graphic(), VisionReceiver<FaceObject> {

    private val radius = context.resources.getDimension(R.dimen.face_eyes_radius)
    private val stroke = context.resources.getDimension(R.dimen.face_eyes_stroke_width)

    private val eyesPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        isAntiAlias = true
    }

    private var face: FaceObject? = null

    init {
        setBoundingBoxColors()
    }

    private fun setBoundingBoxColors() {
        eyesPaint.apply {
            color = Color.RED
            alpha = 50
        }
    }

    override fun onDraw(canvas: Canvas) {
    }

    override fun send(value: FaceObject) {
        face = value
    }
}