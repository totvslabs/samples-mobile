package com.totvs.camera.vision.face

import android.util.Log
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
import com.totvs.camera.vision.utils.exclusiveUse
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
 * This detector relies on the new Firebase detection API.
 *
 * @see [VisionDetector]
 */
open class FaceDetector(
    private val selectFace: SelectionStrategy<FirebaseVisionFace> = MOST_PROMINENT
) : AbstractVisionDetector<FaceObject>(FaceDetector) {

    @NeedsProfiling(
        what = """
        We need to check if running these callbacks on the main thread does have an impact.
        addOnSuccessListener ...
    """
    )
    override fun detect(image: ImageProxy, onDetected: (FaceObject) -> Unit) {
        if (null == image.image) {
            return onDetected(NullFaceObject)
        }

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(getDetectorOptions())

        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val visionImage = image.exclusiveUse {
            FirebaseVisionImage.fromMediaImage(
                image.image!!,
                image.imageInfo.rotationDegrees.toFirebaseVisionRotation()
            )
        }

        detector.detectInImage(visionImage)
            .addOnSuccessListener { faces ->
                // to chose the best face.
                onDetected(
                    if (faces.isEmpty()) NullFaceObject else mapToFaceObject(
                        selectFace(faces)
                    )
                )
            }
            .addOnFailureListener {
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
    open fun getDetectorOptions(): FirebaseVisionFaceDetectorOptions = fastModeOptions()

    /**
     * Readable name
     */
    override fun toString() = TAG

    companion object : VisionDetector.Key<FaceDetector> {

        private const val TAG = "FaceDetector"

        private fun fastModeOptions(): FirebaseVisionFaceDetectorOptions = Builder()
            .setClassificationMode(ALL_CLASSIFICATIONS)
            .setLandmarkMode(ALL_LANDMARKS)
            .build()

        /**
         * Strategy for selecting the most prominent face
         */
        val MOST_PROMINENT = object : SelectionStrategy<FirebaseVisionFace> {
            override fun invoke(faces: List<FirebaseVisionFace>): FirebaseVisionFace =
                faces.first()
        }
    }
}