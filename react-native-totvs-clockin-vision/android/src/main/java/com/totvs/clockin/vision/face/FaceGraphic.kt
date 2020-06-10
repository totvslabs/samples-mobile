package com.totvs.clockin.vision.face

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Size
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.*
import com.totvs.camera.vision.stream.VisionReceiver
import com.totvs.clockin.vision.R

class FaceGraphic(
    context: Context,
    var drawEyes: Boolean = false,
    var drawNose: Boolean = false
) : GraphicOverlay.Graphic(), VisionReceiver<FaceObject> {

    private val radius = context.resources.getDimension(R.dimen.face_landmark_radius)
    private val stroke = context.resources.getDimension(R.dimen.face_landmark_stroke_width)

    private val paint = Paint().apply {
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
        paint.apply {
            color = Color.WHITE
            alpha = 180
        }
    }

    override fun onDraw(canvas: Canvas) {
        face?.let { face ->
            // val nose = face[Nose | LeftEye | RightEye] // try this too.

            face.forEach { landmark ->
                if (drawEyes && (landmark is LeftEye || landmark is RightEye)) {
                    val cx = translateX(landmark.position.x, face.sourceSize)
                    val cy = translateY(landmark.position.y, face.sourceSize)

                    canvas.drawCircle(cx, cy, radius, paint)
                }

                if (drawNose && landmark is Nose) {
                    val cx = translateX(landmark.position.x, face.sourceSize)
                    val cy = translateY(landmark.position.y, face.sourceSize)

                    canvas.drawCircle(cx, cy, radius, paint)
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