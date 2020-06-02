package com.totvs.camera.vision.barcode

import android.util.Log
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
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.face.FaceDetector
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.NullFaceObject
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
    private val selectBarcode: SelectionStrategy<FirebaseVisionBarcode> = MOST_PROMINENT
) : AbstractVisionDetector<BarcodeObject>(BarcodeDetector) {

    @NeedsProfiling(
        what = """
        We need to check if running these callbacks on the main thread does have an impact.
        addOnSuccessListener ...
    """
    )
    override fun detect(
        executor: Executor,
        image: ImageProxy,
        onDetected: (BarcodeObject) -> Unit
    ) {
        if (image.image == null) {
            return onDetected(NullBarcodeObject)
        }
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(getDetectorOptions())

        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val visionImage = image.exclusiveUse {
            FirebaseVisionImage.fromMediaImage(
                image.image!!,
                image.imageInfo.rotationDegrees.toFirebaseVisionRotation()
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
                            selectBarcode(barcodes)
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
    private fun mapToBarcodeObject(barcode: FirebaseVisionBarcode) = BarcodeObject(
        format = barcode.format,
        boundingBox = barcode.boundingBox,
        displayValue = barcode.displayValue ?: ""
    )

    /**
     * Map the firebase vision object to barcode object
     */
    private fun getDetectorOptions() = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(mapBarcodeFormat(primaryFormat), *mapBarcodeFormats(*formats))
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
        private fun mapBarcodeFormats(@BarcodeFormat vararg types: Int): IntArray {
            return types.map { type ->
                when (type) {
                    VisionBarcodeFormat.QR_CODE -> FirebaseVisionBarcode.FORMAT_QR_CODE
                    VisionBarcodeFormat.AZTEC -> FirebaseVisionBarcode.FORMAT_AZTEC
                    else -> throw IllegalArgumentException("Invalid barcode type $type")
                }
            }.toIntArray()
        }

        private fun mapBarcodeFormat(@BarcodeFormat format: Int) = mapBarcodeFormats(format)[0]

        /**
         * Strategy for selecting the most prominent barcode
         */
        val MOST_PROMINENT = object : SelectionStrategy<FirebaseVisionBarcode> {
            override fun invoke(barcodes: List<FirebaseVisionBarcode>): FirebaseVisionBarcode =
                barcodes.first()
        }
    }
}