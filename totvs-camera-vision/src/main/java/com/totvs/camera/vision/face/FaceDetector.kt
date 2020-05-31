package com.totvs.camera.vision.face

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.*
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.core.annotations.NeedsProfiling
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.face.FaceSelection.MOST_PROMINENT
import com.totvs.camera.vision.utils.toFirebaseVisionRotation

/**
 * Detector dedicated to identity faces. This detector is a _Single emission_ detector.
 *
 * This detector is focused in classifications and landmark detections, following the
 * recommendations on:
 * https://firebase.google.com/docs/ml-kit/android/detect-faces#performance_tips
 *
 * If a contour or another kind of detector is required, this detector offers an interface
 * for subclasses to customize the detector capabilities.
 *
 * As-is this detector keeps a singleton of a high-accuracy detector.
 *
 * @see [VisionDetector]
 */
open class FaceDetector(
    private val selectFace: SelectionStrategy<FirebaseVisionFace> = MOST_PROMINENT
) : AbstractVisionDetector<FaceObject>(FaceDetector) {

    @NeedsProfiling(what = "We need to profile how expensive is to create a face detector")
    override fun detect(image: ImageProxy, onDetected: (FaceObject) -> Unit) {
        if (null == image.image) {
            return onDetected(NullFaceObject)
        }
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(getDetectorOptions())

        val visionImage = FirebaseVisionImage.fromMediaImage(
            image.image!!,
            image.imageInfo.rotationDegrees.toFirebaseVisionRotation()
        )

        detector.detectInImage(visionImage)
            .addOnSuccessListener { faces ->
                // we close the used image: MUST DO
                closeImage(image)
                // to chose the best face.
                onDetected(mapToFaceObject(selectFace(faces)))
            }
            .addOnFailureListener {
                // we close the used image: MUST DO
                closeImage(image)

                onDetected(NullFaceObject)
            }
    }
    /**
     * Map the firebase vision object to face object
     */
    open fun mapToFaceObject(face: FirebaseVisionFace): FaceObject {
        return FaceObject()
    }

    /**
     * Get the detector used for this instance of the face detector.
     */
    open fun getDetectorOptions(): FirebaseVisionFaceDetectorOptions = highAccuracyOptions()

    companion object : VisionDetector.Key<FaceDetector> {
        private fun highAccuracyOptions(): FirebaseVisionFaceDetectorOptions =
            FirebaseVisionFaceDetectorOptions.Builder()
                .setClassificationMode(ALL_CLASSIFICATIONS)
                .setPerformanceMode(ACCURATE)
                .setLandmarkMode(ALL_LANDMARKS)
                .build()
    }
}