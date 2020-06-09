package com.totvs.clockin.vision.core

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.Keep

/**
 * Clock-In specific purpose face object. This model class conforms specific
 * IPC format used for faces.
 */
@Keep
abstract class Face {
    /**
     * Label of the face object. meta-info
     */
    abstract val label: String?

    /**
     * Confidence of this face object. this might mean confidence of detection
     * or confidence of recognition.
     */
    abstract val confidence: Float

    /**
     * Bounding box determining this face on the provided source where this face object
     * was detected/recognized
     */
    abstract val boundingBox: Rect

    /**
     * Landmark points for this face object
     */
    abstract val landmarkPoints: List<Point>

    /**
     * Encoding of the face. meta-info
     */
    abstract val encoding: String?
}