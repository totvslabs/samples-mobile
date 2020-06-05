package com.totvs.camera.vision.barcode

import android.util.Log
import android.util.Size
import androidx.core.graphics.toRectF
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.annotations.BarcodeFormat
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.utils.exclusiveUse
import com.totvs.camera.vision.utils.toFirebaseVisionRotation
import java.util.concurrent.Executor

/**
 * Detector dedicated to identity barcode. This detector is a _Single emission_ detector.
 *
 * This barcode detector detect [primaryFormat] as mandatory code. Firebase
 * forces us to chose at least one code to detect.
 *
 * @see [VisionDetector]
 */
class BarcodeDetector(
    @BarcodeFormat private val primaryFormat: Int = VisionBarcodeFormat.QR_CODE,
    @BarcodeFormat private vararg val formats: Int,
    private val selectBarcode: SelectionStrategy<FirebaseVisionBarcode> = FIRST
) : AbstractVisionDetector<BarcodeObject>(BarcodeDetector) {

    override fun detect(
        executor: Executor,
        image: ImageProxy,
        onDetected: (BarcodeObject) -> Unit
    ) {
        if (image.image == null) {
            return onDetected(NullBarcodeObject)
        }
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(getDetectorOptions())

        val rotation = image.imageInfo.rotationDegrees

        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val visionImage = image.exclusiveUse {
            FirebaseVisionImage.fromMediaImage(
                image.image!!,
                rotation.toFirebaseVisionRotation()
            )
        }

        // we perform this manual execution instead of passing the executor to Firebase
        // because if the executor is shut down before this callback is called,
        // Firebase will popup the exception, here instead we log it

        detector.detectInImage(visionImage)
            .addOnSuccessListener { barcodes ->
                executor.executeCatching(onDetected) {
                    onDetected(
                        if (barcodes.isEmpty()) NullBarcodeObject else mapToBarcodeObject(
                            selectBarcode(barcodes),
                            rotation,
                            Size(image.width, image.height)
                        )
                    )
                }
            }
            .addOnFailureListener {
                executor.executeCatching(onDetected) { onDetected(NullBarcodeObject) }
            }
    }

    /**
     * Map the firebase vision object to face object
     */
    private fun mapToBarcodeObject(
        barcode: FirebaseVisionBarcode,
        rotation: Int,
        sourceSize: Size
    ) = BarcodeObject(
        sourceSize = sourceSize,
        boundingBox = barcode.boundingBox?.toRectF(),
        sourceRotationDegrees = rotation,
        format = fromFirebaseFormat(barcode.format),
        displayValue = barcode.displayValue ?: ""
    )

    /**
     * Map the firebase vision object to barcode object
     */
    private fun getDetectorOptions() = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(toFirebaseFormat(primaryFormat), *toFirebaseFormats(*formats))
        .build()

    /**
     * Readable name
     */
    override fun toString() = TAG

    /**
     * Utility method to run safely on the executor a blocks
     */
    protected fun Executor.executeCatching(
        onDetected: (BarcodeObject) -> Unit,
        block: () -> Unit
    ) = this.runCatching {
        execute(block)
    }.exceptionOrNull()?.let { ex ->
        if (DEBUG_ENABLED) {
            Log.e(TAG, "", ex)
        }
        onDetected(NullBarcodeObject)
    }

    companion object : VisionDetector.Key<BarcodeDetector> {
        private const val TAG = "BarcodeDetector"

        /**
         * Map [VisionBarcodeFormat] to [FirebaseVisionBarcode] constants
         */
        private fun toFirebaseFormats(@BarcodeFormat vararg formats: Int): IntArray {
            return formats.map { format ->
                when (format) {
                    VisionBarcodeFormat.QR_CODE -> FirebaseVisionBarcode.FORMAT_QR_CODE
                    VisionBarcodeFormat.AZTEC -> FirebaseVisionBarcode.FORMAT_AZTEC
                    else -> throw IllegalArgumentException("Invalid barcode type $format")
                }
            }.toIntArray()
        }

        private fun toFirebaseFormat(@BarcodeFormat format: Int) = toFirebaseFormats(format)[0]

        private fun fromFirebaseFormat(format: Int) = when (format) {
            FirebaseVisionBarcode.FORMAT_QR_CODE -> VisionBarcodeFormat.QR_CODE
            FirebaseVisionBarcode.FORMAT_AZTEC -> VisionBarcodeFormat.AZTEC
            else -> throw IllegalArgumentException("Invalid barcode type $format")
        }

        /**
         * Strategy for selecting the first barcode
         */
        val FIRST = object : SelectionStrategy<FirebaseVisionBarcode> {
            override fun invoke(barcodes: List<FirebaseVisionBarcode>): FirebaseVisionBarcode =
                barcodes.first()
        }
    }
}