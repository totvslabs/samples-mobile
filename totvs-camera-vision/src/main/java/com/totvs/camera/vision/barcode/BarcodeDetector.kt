package com.totvs.camera.vision.barcode

import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector

/**
 * Detector dedicated to identity barcode
 */
class BarcodeDetector : AbstractVisionDetector<BarcodeObject>(BarcodeDetector){

    override fun detect(image: ImageProxy, onDetected: (BarcodeObject) -> Unit) {

    }

    companion object : VisionDetector.Key<BarcodeDetector>
}