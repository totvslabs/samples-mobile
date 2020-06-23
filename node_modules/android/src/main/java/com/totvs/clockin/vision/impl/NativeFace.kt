package com.totvs.clockin.vision.impl

import android.graphics.Point
import android.graphics.Rect
import com.totvs.clockin.vision.face.Face
import com.tzutalin.dlib.VisionDetRet

internal class NativeFace(private val rect: VisionDetRet) : Face() {
    override val label: String?
        get() = rect.label

    override val confidence: Float
        get() = rect.confidence

    override val boundingBox: Rect
        get() = Rect(rect.left, rect.top, rect.right, rect.bottom)

    override val landmarkPoints: List<Point>
        get() = rect.faceLandmarks?.toList() ?: emptyList()

    override val encoding: String?
        get() = rect.faceEncodings
}