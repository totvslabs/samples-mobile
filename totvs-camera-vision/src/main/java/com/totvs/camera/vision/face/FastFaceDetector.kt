package com.totvs.camera.vision.face

import android.content.Context
import android.graphics.ImageFormat
import android.util.Log
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
 * This detector relies on the old GMs Mobile Vision detection API, which proved to be faster
 * than the new implementation.
 *
 * @see [VisionDetector]
 */
class FastFaceDetector(
    private val context: Context,
    private val selectFace: SelectionStrategy<Face> = MOST_PROMINENT
) : AbstractVisionDetector<FaceObject>(FastFaceDetector) {

    override fun detect(executor: Executor, image: ImageProxy, onDetected: (FaceObject) -> Unit) {
        if (image.image == null) {
            return onDetected(NullFaceObject)
        }
        // we require to use this image exclusively and nobody else can read the data until
        // we're done with it.
        val frame = image.exclusiveUse {
            Frame.Builder()
                .setImageData(
                    Images.YUV_420_888toNV21(image.image!!),
                    image.width,
                    image.height,
                    ImageFormat.NV21
                )
                .setRotation(image.imageInfo.rotationDegrees.toFirebaseVisionRotation())
                .build()
        }

        executor.executeCatching(onDetected) {
            val faces = getDetector(context).detect(frame)
            if (faces.isEmpty()) {
                onDetected(NullFaceObject)
            } else {
                onDetected(FaceObject())
            }
        }
    }

    /**
     * Map the firebase vision object to face object
     */
    open fun mapToFaceObject(face: Face): FaceObject {
        return FaceObject()
    }

    /**
     * Get the detector used for this instance of the face detector.
     */
    open fun getDetector(context: Context): FaceDetector = highAccuracyDetector(context)

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