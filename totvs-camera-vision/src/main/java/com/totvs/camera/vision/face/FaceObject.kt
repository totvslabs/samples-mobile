package com.totvs.camera.vision.face

import android.graphics.RectF
import android.util.Size
import com.totvs.camera.vision.VisionObject

/**
 * Vision object that represents a detected barcode.
 *
 * Encoded information on this object, depends on the kind of detector used
 */
data class FaceObject(
    override val sourceSize: Size = Size(0, 0),
    override val boundingBox: RectF? = null,
    override val sourceRotationDegrees: Int = -1
) : VisionObject()

/**
 * Null representation of a null face object
 */
val NullFaceObject = FaceObject()