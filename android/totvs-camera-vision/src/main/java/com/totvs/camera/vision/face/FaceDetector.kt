package com.totvs.camera.vision.face

import android.graphics.PointF
import android.util.Log
import android.util.Size
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.*
import com.google.mlkit.vision.face.FaceLandmark
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.utils.exclusiveUse
import java.util.concurrent.Executor

/**
 * Detector dedicated to identity faces. This detector is a _Single emission_ detector.
 *
 * This detector is focused in classifications and landmark detections, following the
 * recommendations on:
 * https://developers.google.com/ml-kit/vision/face-detection/android#performance_tips
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
    private val selectFace: SelectionStrategy<Face> = FIRST
) : AbstractVisionDetector<FaceObject>(FaceDetector) {

    private val detector by lazy { FaceDetection.getClient(getDetectorOptions()) }

    override fun detect(
        executor: Executor,
        image: ImageProxy,
        onDetected: (FaceObject) -> Unit
    ) {
        if (null == image.image) {
            return onDetected(NullFaceObject)
        }
        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val inputImage = image.exclusiveUse {
            InputImage.fromMediaImage(
                it.image!!,
                it.imageInfo.rotationDegrees
            )
        }

        // we perform manual executor execution instead of passing the executor to Firebase
        // because if the executor is shut down before this callback is called,
        // Firebase will popup the exception, here instead we log it
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                // to chose the best face.
                executor.executeCatching(onDetected) {
                    onDetected(
                        if (faces.isEmpty()) NullFaceObject else mapToFaceObject(
                            selectFace(faces),
                            image.imageInfo.rotationDegrees,
                            Size(image.width, image.height)
                        )
                    )
                }
            }
            .addOnFailureListener {
                executor.executeCatching(onDetected) { onDetected(NullFaceObject) }
            }
    }

    /**
     * Readable name
     */
    override fun toString() = TAG

    /**
     * Get the detector options used for this instance of the face detector.
     */
    open fun getDetectorOptions(): FaceDetectorOptions = fastModeOptions()

    /**
     * Map the MLKIT vision face to face object
     */
    open fun mapToFaceObject(
        face: Face,
        rotation: Int,
        sourceSize: Size
    ) = FaceObject(
        sourceSize = sourceSize,
        boundingBox = face.boundingBox.toRectF(),
        sourceRotationDegrees = rotation,
        width = face.boundingBox.width().toFloat(),
        height = face.boundingBox.height().toFloat(),
        eyesOpenProbability = EyesOpenProbability(
            face.leftEyeOpenProbability   ?: 0.0f,
            face.rightEyeOpenProbability ?: 0.0f
        ),
        eulerZ = face.headEulerAngleZ,
        eulerY = face.headEulerAngleY,
        landmarks = extractLandmarks(face)
    )

    /**
     * Extract all the recognized landmarks. We consider a landmark as recognized
     * if there's a corespondent type [Landmark] for it. If there isn't then the landmark is
     * ignored
     */
    protected fun extractLandmarks(face: Face): List<Landmark> {
        val landmarks = mutableListOf<Landmark>()
        // left eye
        face.getLandmark(FaceLandmark.LEFT_EYE)?.let {
            landmarks.add(LeftEye(PointF(it.position.x, it.position.y)))
        }
        // right eye
        face.getLandmark(FaceLandmark.RIGHT_EYE)?.let {
            landmarks.add(RightEye(PointF(it.position.x, it.position.y)))
        }
        face.getLandmark(FaceLandmark.NOSE_BASE)?.let {
            landmarks.add(Nose(PointF(it.position.x, it.position.y)))
        }
        return landmarks
    }

    /**
     * Utility method to run safely on the executor a blocks
     */
    protected fun Executor.executeCatching(
        onDetected: (FaceObject) -> Unit,
        block: () -> Unit
    ) = execute {
        runCatching(block).exceptionOrNull()?.let {
            if (DEBUG_ENABLED) {
                Log.e(TAG, "", it)
            }
            onDetected(NullFaceObject)
        }
    }

    companion object : VisionDetector.Key<FaceDetector> {

        private const val TAG = "FaceDetector"

        private fun fastModeOptions(): FaceDetectorOptions = Builder()
            .setClassificationMode(CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(LANDMARK_MODE_ALL)
            .setContourMode(CONTOUR_MODE_NONE)
            .setPerformanceMode(PERFORMANCE_MODE_FAST)
            .build()

        /**
         * Strategy for selecting the first face
         */
        val FIRST = object : SelectionStrategy<Face> {
            override fun invoke(faces: List<Face>): Face =
                faces.first()
        }
    }
}