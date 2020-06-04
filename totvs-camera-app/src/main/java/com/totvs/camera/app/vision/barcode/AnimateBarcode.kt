package com.totvs.camera.app.vision.barcode

import android.graphics.RectF
import com.totvs.camera.app.vision.InterpolateBounds
import com.totvs.camera.vision.barcode.BarcodeObject

/**
 * Animate [BarcodeObject]s
 */
class AnimateBarcode : InterpolateBounds<BarcodeObject>() {
    override fun BarcodeObject.clone(
        boundingBox: RectF?
    ) = copy(boundingBox = boundingBox)
}