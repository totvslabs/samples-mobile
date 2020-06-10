package com.totvs.clockin.vision.face

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Size
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.LeftEye
import com.totvs.camera.vision.face.RightEye
import com.totvs.camera.vision.face.isNull
import com.totvs.camera.vision.stream.VisionReceiver
import com.totvs.clockin.vision.R

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

    fun clear() {
        face = null
        postInvalidate()
    }

    private fun setBoundingBoxColors() {
        eyesPaint.apply {
            color = Color.WHITE
            alpha = 180
        }
    }

    override fun onDraw(canvas: Canvas) {
        face?.let { face ->
            // third way
            face.forEach { landmark ->
                if (landmark is LeftEye || landmark is RightEye) {
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