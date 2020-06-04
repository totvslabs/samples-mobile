package com.totvs.camera.app.vision.barcode

import android.graphics.RectF
import android.util.Size
import com.totvs.camera.app.vision.TranslateBounds
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.barcode.BarcodeObject

class TranslateBarcode(
    overlay: GraphicOverlay
) : TranslateBounds<BarcodeObject>(overlay) {

    override fun BarcodeObject.clone(
        sourceSize: Size,
        boundingBox: RectF?
    ): BarcodeObject = copy(
        sourceSize = sourceSize,
        boundingBox = boundingBox
    )
}