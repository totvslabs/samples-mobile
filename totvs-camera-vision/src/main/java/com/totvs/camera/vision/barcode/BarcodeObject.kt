package com.totvs.camera.vision.barcode

import android.graphics.RectF
import android.util.Size
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.annotations.BarcodeFormat

/**
 * Vision object that represents a detected barcode.
 *
 * Encoded information on this object, depends on the kind of detector used. This entity can
 * be extended as the application needs it.
 *
 * @param format
 */
data class BarcodeObject(
    override val sourceSize: Size = Size(0, 0),
    override val boundingBox: RectF? = null,
    override val sourceRotationDegrees: Int = -1,
    @BarcodeFormat val format: Int = -1,
    val displayValue: String = ""
) : VisionObject()

/**
 * Null representation of barcode
 */
val NullBarcodeObject = BarcodeObject(sourceSize = Size(Int.MIN_VALUE, Int.MIN_VALUE))

/**
 * Accessor to know when this [VisionObject] is null
 */
val BarcodeObject.isNull get() = this == NullBarcodeObject