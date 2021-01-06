package com.totvs.clockin.vision.internal

import android.graphics.Point
import android.graphics.Rect
import com.totvs.clockin.vision.face.Face

internal class NativeFace : Face() {
    override val label: String?
        get() = TODO()

    override val confidence: Float
        get() = TODO()

    override val boundingBox: Rect
        get() = TODO()

    override val landmarkPoints: List<Point>
        get() = TODO()

    override val encoding: String?
        get() = TODO()
}