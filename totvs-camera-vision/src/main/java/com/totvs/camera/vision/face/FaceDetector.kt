package com.totvs.camera.vision.face

import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector

/**
 * Detector dedicated to identity faces
 */
class FaceDetector : AbstractVisionDetector<FaceObject>(FaceDetector) {

    override fun detect(image: ImageProxy, onDetected: (FaceObject) -> Unit) {
    }

    companion object : VisionDetector.Key<FaceDetector>
}