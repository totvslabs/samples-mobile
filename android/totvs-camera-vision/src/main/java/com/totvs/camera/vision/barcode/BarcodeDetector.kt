package com.totvs.camera.vision.barcode

import android.util.Log
import android.util.Size
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.annotations.BarcodeFormat
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.utils.exclusiveUse
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
    private val selectBarcode: SelectionStrategy<Barcode> = FIRST
) : AbstractVisionDetector<BarcodeObject>(BarcodeDetector) {

    private val detector by lazy { BarcodeScanning.getClient(getDetectorOptions()) }

    override fun detect(
        executor: Executor,
        image: ImageProxy,
        onDetected: (BarcodeObject) -> Unit
    ) {
        if (image.image == null) {
            return onDetected(NullBarcodeObject)
        }
        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val inputImage = image.exclusiveUse {
            InputImage.fromMediaImage(
                image.image!!,
                image.imageInfo.rotationDegrees
            )
        }

        // we perform this manual execution instead of passing the executor to Firebase
        // because if the executor is shut down before this callback is called,
        // Firebase will popup the exception, here instead we log it

        detector.process(inputImage)
            .addOnSuccessListener { barcodes ->
                executor.executeCatching(onDetected) {
                    onDetected(
                        if (barcodes.isEmpty()) NullBarcodeObject else mapToBarcodeObject(
                            selectBarcode(barcodes),
                            image.imageInfo.rotationDegrees,
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
     * Map the MLKIT vision barcode to barcode object
     */
    private fun mapToBarcodeObject(
        barcode: Barcode,
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
     * Get the detector options used for this instance of the barcode detector.
     */
    private fun getDetectorOptions() = BarcodeScannerOptions.Builder()
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
         * Map [VisionBarcodeFormat] to [Barcode] constants
         */
        private fun toFirebaseFormats(@BarcodeFormat vararg formats: Int): IntArray {
            return formats.map { format ->
                when (format) {
                    VisionBarcodeFormat.QR_CODE -> Barcode.FORMAT_QR_CODE
                    VisionBarcodeFormat.AZTEC -> Barcode.FORMAT_AZTEC
                    else -> throw IllegalArgumentException("Invalid barcode type $format")
                }
            }.toIntArray()
        }

        private fun toFirebaseFormat(@BarcodeFormat format: Int) = toFirebaseFormats(format)[0]

        private fun fromFirebaseFormat(format: Int) = when (format) {
            Barcode.FORMAT_QR_CODE -> VisionBarcodeFormat.QR_CODE
            Barcode.FORMAT_AZTEC -> VisionBarcodeFormat.AZTEC
            else -> throw IllegalArgumentException("Invalid barcode type $format")
        }

        /**
         * Strategy for selecting the first barcode
         */
        val FIRST = object : SelectionStrategy<Barcode> {
            override fun invoke(barcodes: List<Barcode>): Barcode =
                barcodes.first()
        }
    }
}