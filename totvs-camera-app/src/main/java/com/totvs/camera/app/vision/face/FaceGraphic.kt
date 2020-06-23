package com.totvs.camera.app.vision.face

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Size
import com.totvs.camera.app.R
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.*
import com.totvs.camera.vision.stream.VisionReceiver

class FaceGraphic(
    context: Context
) : GraphicOverlay.Graphic(), VisionReceiver<FaceObject> {

    private val radius = context.resources.getDimension(R.dimen.face_eyes_radius)
    private val stroke = context.resources.getDimension(R.dimen.face_eyes_stroke_width)

    private val eyesPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = stroke
        isAntiAlias = true
    }

    private var face: FaceObject? = null

    init {
        setBoundingBoxColors()
    }

    private fun setBoundingBoxColors() {
        eyesPaint.apply {
            color = Color.WHITE
            alpha = 180
        }
    }


    override fun onDraw(canvas: Canvas) {
        face?.let { face ->
            // one way of retrieving the eyes
            // val lEye = face[LeftEye]
            // val rEye = face[RightEye]

            // second way
            // for (landmark in face) { ... }

            // third way
            face.forEach { landmark ->
                if (landmark is LeftEye || landmark is RightEye || landmark is Nose) {
                    val cx = translateX(landmark.position.x, face.sourceSize)
                    val cy = translateY(landmark.position.y, face.sourceSize)

                    canvas.drawCircle(cx, cy, radius, eyesPaint)
                }
            }
        }
    }

    override fun send(value: FaceObject) {
        face = if (value.isNull) {
            null
        } else {
            val rotatedSize = when (value.sourceRotationDegrees) {
                0, 180 -> value.sourceSize
                90, 270 -> Size(value.sourceSize.height, value.sourceSize.width)
                else -> throw IllegalArgumentException("Valid rotation are 0, 90, 180, 270")
            }
            // @TODO special to the mirroring must be placed here. but since we're only drawing eyes.
            //       we can skip that additional computation.
            value.copy(sourceSize = rotatedSize)
        }
        postInvalidate()
    }
}