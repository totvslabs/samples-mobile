package com.totvs.camera.vision.face

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.*
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.utils.exclusiveUse
import com.totvs.camera.vision.utils.toFirebaseVisionRotation
import java.util.concurrent.Executor

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

    override fun detect(executor: Executor, image: ImageProxy, onDetected: (FaceObject) -> Unit) {
        if (null == image.image) {
            return onDetected(NullFaceObject)
        }

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(getDetectorOptions())

        val rotation = image.imageInfo.rotationDegrees

        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val visionImage = image.exclusiveUse {
            FirebaseVisionImage.fromMediaImage(
                image.image!!,
                rotation.toFirebaseVisionRotation()
            )
        }

        // we perform manual executor execution instead of passing the executor to Firebase
        // because if the executor is shut down before this callback is called,
        // Firebase will popup the exception, here instead we log it

        detector.detectInImage(visionImage)
            .addOnSuccessListener { faces ->
                // to chose the best face.
                executor.executeCatching(onDetected) {
                    onDetected(
                        if (faces.isEmpty()) NullFaceObject else mapToFaceObject(
                            rotation,
                            selectFace(faces)
                        )
                    )
                }
            }
            .addOnFailureListener {
                executor.executeCatching(onDetected) { onDetected(NullFaceObject) }
            }
    }

    /**
     * Map the firebase vision object to face object
     */
    open fun mapToFaceObject(rotation: Int, face: FirebaseVisionFace): FaceObject {
        return FaceObject(sourceRotationDegrees = rotation)
    }

    /**
     * Get the detector used for this instance of the face detector.
     */
    open fun getDetectorOptions(): FirebaseVisionFaceDetectorOptions = fastModeOptions()

    /**
     * Readable name
     */
    override fun toString() = TAG

    /**
     * Utility method to run safely on the executor a blocks
     */
    protected fun Executor.executeCatching(
        onDetected: (FaceObject) -> Unit,
        block: () -> Unit
    ) = this.runCatching {
        execute(block)
    }.exceptionOrNull()?.let { ex ->
        if (DEBUG_ENABLED) {
            Log.e(TAG, "", ex)
        }
        onDetected(NullFaceObject)
    }

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