package com.totvs.camera.vision.face

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.RectF
import android.util.Log
import android.util.Size
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.AbstractVisionDetector
import com.totvs.camera.vision.VisionDetector
import com.totvs.camera.vision.core.SelectionStrategy
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.utils.Images
import com.totvs.camera.vision.utils.exclusiveUse
import com.totvs.camera.vision.utils.toImageFrameRotation
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
 * This detector relies on the old GMs Mobile Vision detection API, which proved to be faster
 * than the new implementation.
 *
 * @see [VisionDetector]
 */

private typealias GMSLandmark = com.google.android.gms.vision.face.Landmark

open class FastFaceDetector(
    private val context: Context,
    private val selectFace: SelectionStrategy<Face> = MOST_PROMINENT
) : AbstractVisionDetector<FaceObject>(FastFaceDetector) {

    override fun detect(
        executor: Executor,
        image: ImageProxy,
        onDetected: (FaceObject) -> Unit
    ) {
        if (image.image == null) {
            return onDetected(NullFaceObject)
        }
        val rotation = image.imageInfo.rotationDegrees
        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val frame = image.exclusiveUse {
            Frame.Builder()
                .setImageData(
                    Images.YUV_420_888toNV21(image.image!!),
                    image.width,
                    image.height,
                    ImageFormat.NV21
                ).setRotation(rotation.toImageFrameRotation()).build()
        }

        executor.executeCatching(onDetected) {
            val faces = getDetector(context).detect(frame)
            onDetected(
                if (faces.isEmpty()) NullFaceObject else mapToFaceObject(
                    selectFace(faces.toList()),
                    rotation,
                    Size(image.width, image.height)
                )
            )
        }
    }

    /**
     * Readable name
     */
    override fun toString() = TAG

    /**
     * Get the detector used for this instance of the face detector.
     */
    open fun getDetector(context: Context): FaceDetector = highAccuracyDetector(context)


    /**
     * Map the MLKIT vision face to face object
     */
    open fun mapToFaceObject(
        face: Face,
        rotation: Int,
        sourceSize: Size
    ) = FaceObject(
        sourceSize = sourceSize,
        boundingBox = RectF(
            face.position.x, face.position.y,
            face.position.x + face.width,
            face.position.y + face.height
        ),
        sourceRotationDegrees = rotation,
        width = face.width,
        height = face.height,
        eyesOpenProbability = EyesOpenProbability(
            face.isLeftEyeOpenProbability,
            face.isRightEyeOpenProbability
        ),
        eulerY = face.eulerY,
        eulerZ = face.eulerZ,
        landmarks = extractLandmarks(face)
    )

    /**
     * Extract all the recognized landmarks. We consider a landmark as recognized
     * if there's a corespondent type [Landmark] for it. If there isn't then the landmark is
     * ignored
     */
    protected fun extractLandmarks(face: Face): List<Landmark> {
        val landmarks = mutableListOf<Landmark>()
        face.landmarks.forEach { l ->
            if (l.type == GMSLandmark.LEFT_EYE) {
                landmarks.add(LeftEye(l.position))
            }
            if (l.type == GMSLandmark.RIGHT_EYE) {
                landmarks.add(RightEye(l.position))
            }
            if (l.type == GMSLandmark.NOSE_BASE) {
                landmarks.add(Nose(l.position))
            }
        }
        return landmarks
    }

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

    /**
     * Convert an sparse array to list
     */
    private fun <T> SparseArray<T>.toList() = mutableListOf<T>().let {
        forEach { _, value -> it.add(value) }
        it
    }

    companion object : VisionDetector.Key<FastFaceDetector> {

        private const val TAG = "FastFaceDetector"

        private lateinit var detector: FaceDetector

        /**
         * Returns a configured detector
         */
        private fun highAccuracyDetector(context: Context) =
            if (::detector.isInitialized) detector else {
                FaceDetector.Builder(context)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .setProminentFaceOnly(true)
                    .setMinFaceSize(.35f)
                    .setTrackingEnabled(false)
                    .setMode(FaceDetector.ACCURATE_MODE)
                    .build().also {
                        detector = it
                    }
            }

        /**
         * Strategy for selecting the most prominent face
         */
        val MOST_PROMINENT = object : SelectionStrategy<Face> {
            override fun invoke(faces: List<Face>): Face = faces.first()
        }
    }
}