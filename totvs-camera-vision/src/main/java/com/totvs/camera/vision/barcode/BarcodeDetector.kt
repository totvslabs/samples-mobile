package com.totvs.camera.vision.barcode

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.core.annotations.NeedsProfiling
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.annotations.BarcodeFormat
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.camera.vision.utils.toFirebaseVisionRotation

/**
 * Detector dedicated to identity barcode. This barcode detector detect [primaryType]
 * as mandatory code. Firebase forces us to chose at least one code to detect.
 */
class BarcodeDetector(
    @BarcodeFormat private val primaryType: Int,
    @BarcodeFormat private vararg val types: Int
) : AbstractVisionDetector<BarcodeObject>(BarcodeDetector) {

    @NeedsProfiling(
        what = """
        We need to profile how expensive is to create a barcode detector, this will allows
        us to also determine the appropriate technique to change at runtime the 
        barcode types that this detector is capable of detect,
    """
    )
    override fun detect(image: ImageProxy, onDetected: (BarcodeObject) -> Unit) {
        if (image.image == null) {
            return onDetected(NullBarcodeObject)
        }
        val detector = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(
                FirebaseVisionBarcodeDetectorOptions
                    .Builder()
                    .setBarcodeFormats(primaryType, *mapBarcodeFormats(*types))
                    .build()
            )
        val visionImage = FirebaseVisionImage.fromMediaImage(
            image.image!!,
            image.imageInfo.rotationDegrees.toFirebaseVisionRotation()
        )
        detector.detectInImage(visionImage)
            .addOnSuccessListener { barcodes ->
                // chose the first one.
                with(barcodes.first()) {
                    onDetected(BarcodeObject(
                        format = format,
                        boundingBox = boundingBox,
                        displayValue = displayValue ?: ""
                    ))
                }
            }
            .addOnFailureListener {
                onDetected(NullBarcodeObject)
            }
    }

    companion object : VisionDetector.Key<BarcodeDetector> {
        /**
         * Map [VisionBarcodeFormat] to [FirebaseVisionBarcode] constants
         */
        private fun mapBarcodeFormats(@BarcodeFormat vararg types: Int): IntArray {
            return types.map { type ->
                when (type) {
                    VisionBarcodeFormat.QR_CODE -> FirebaseVisionBarcode.FORMAT_QR_CODE
                    VisionBarcodeFormat.AZTEC -> FirebaseVisionBarcode.FORMAT_AZTEC
                    else -> throw IllegalArgumentException("Invalid barcode type $type")
                }
            }.toIntArray()
        }
    }
}