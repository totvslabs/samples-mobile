package com.totvs.camera.vision.barcode

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.totvs.camera.vision.core.SelectionStrategy

/**
 * Selection strategy for detected barcode
 */
enum class BarcodeSelection : SelectionStrategy<FirebaseVisionBarcode> {
    FIRST {
        override fun invoke(
            barcodes: List<FirebaseVisionBarcode>
        ): FirebaseVisionBarcode = barcodes.first()
    },
    MOST_PROMINENT { // @TODO what is most prominent
        override fun invoke(
            barcodes: List<FirebaseVisionBarcode>
        ): FirebaseVisionBarcode = barcodes.first()
    }
}