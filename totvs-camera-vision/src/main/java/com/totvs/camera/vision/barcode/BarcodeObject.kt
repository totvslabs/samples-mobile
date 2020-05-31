package com.totvs.camera.vision.barcode

import android.graphics.Rect
import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.annotations.BarcodeFormat

/**
 * Vision object that represents a detected barcode.
 *
 * Encoded information on this object, depends on the kind of detector used. This entity can
 * be extended as the application needs it.
 */
data class BarcodeObject(
    @BarcodeFormat val format: Int = -1,
    val boundingBox: Rect? = null,
    val displayValue: String = ""
) : VisionObject()

/**
 * Null representation of barcode
 */
val NullBarcodeObject = BarcodeObject()