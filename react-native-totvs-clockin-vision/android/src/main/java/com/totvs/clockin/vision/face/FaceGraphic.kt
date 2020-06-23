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

    private val eyesRadius = context.resources.getDimension(R.dimen.eyes_landmark_radius)
    private val eyesStroke = context.resources.getDimension(R.dimen.eyes_landmark_stroke_width)
    private val noseRadius = context.resources.getDimension(R.dimen.nose_landmark_radius)
    private val noseStroke = context.resources.getDimension(R.dimen.nose_landmark_stroke_width)
    private val eyesColor  = context.resources.getColor(R.color.eyes_landmark_color)
    private val noseColor  = context.resources.getColor(R.color.nose_landmark_color)


    private val eyesPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = eyesStroke
        isAntiAlias = true
        color = eyesColor
    }

    private val nosePaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = noseStroke
        isAntiAlias = true
        color = noseColor
    }

    private var face: FaceObject? = null

    fun clear() {
        face = null
        if (isAttached) {
            postInvalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        face?.let { face ->
            // val nose = face[Nose | LeftEye | RightEye] // try this too.

            face.forEach { landmark ->
                if (drawEyes && (landmark is LeftEye || landmark is RightEye)) {
                    val cx = translateX(landmark.position.x, face.sourceSize)
                    val cy = translateY(landmark.position.y, face.sourceSize)

                    canvas.drawCircle(cx, cy, eyesRadius, eyesPaint)
                }

                if (drawNose && landmark is Nose) {
                    val cx = translateX(landmark.position.x, face.sourceSize)
                    val cy = translateY(landmark.position.y, face.sourceSize)

                    canvas.drawCircle(cx, cy, noseRadius, nosePaint)
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
            // @TODO(jansel) special to the mirroring must be placed here. but since we're only drawing eyes.
            //       we can skip that additional computation.
            value.copy(sourceSize = rotatedSize)
        }
        postInvalidate()
    }
}