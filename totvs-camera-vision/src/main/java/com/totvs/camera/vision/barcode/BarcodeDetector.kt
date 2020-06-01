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
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.camera.vision.utils.toFirebaseVisionRotation

/**
 * Detector dedicated to identity barcode. This detector is a _Single emission_ detector.
 *
 * This barcode detector detect [primaryType] as mandatory code. Firebase
 * forces us to chose at least one code to detect.
 *
 * @see [VisionDetector]
 */
class BarcodeDetector(
    @BarcodeFormat private val primaryType: Int = VisionBarcodeFormat.QR_CODE,
    @BarcodeFormat private vararg val types: Int,
    private val selectBarcode: SelectionStrategy<FirebaseVisionBarcode> = MOST_PROMINENT
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
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(getDetectorOptions())

        val visionImage = FirebaseVisionImage.fromMediaImage(
            image.image!!,
            image.imageInfo.rotationDegrees.toFirebaseVisionRotation()
        )
        detector.detectInImage(visionImage)
            .addOnSuccessListener { barcodes ->
                // we close the used image: MUST DO
                closeImage(image)
                // chose the first one.
                onDetected(
                    if (barcodes.isEmpty()) NullBarcodeObject else mapToBarcodeObject(
                        selectBarcode(barcodes)
                    )
                )
            }
            .addOnFailureListener {
                // we close the used image: MUST DO
                closeImage(image)

                onDetected(NullBarcodeObject)
            }
    }

    /**
     * Map the firebase vision object to face object
     */
    private fun mapToBarcodeObject(barcode: FirebaseVisionBarcode) = BarcodeObject(
        format = barcode.format,
        boundingBox = barcode.boundingBox,
        displayValue = barcode.displayValue ?: ""
    )

    /**
     * Map the firebase vision object to barcode object
     */
    private fun getDetectorOptions() = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(primaryType, *mapBarcodeFormats(*types))
        .build()

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

        /**
         * Strategy for selecting the most prominent barcode
         */
        val MOST_PROMINENT = object : SelectionStrategy<FirebaseVisionBarcode> {
            override fun invoke(barcodes: List<FirebaseVisionBarcode>): FirebaseVisionBarcode =
                barcodes.first()
        }
    }
}