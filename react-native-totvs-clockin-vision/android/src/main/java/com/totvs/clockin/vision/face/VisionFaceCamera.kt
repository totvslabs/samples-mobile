package com.totvs.clockin.vision.face

import android.graphics.Bitmap
import com.totvs.clockin.vision.core.RecognitionModel
import com.totvs.clockin.vision.core.VisionCamera
import java.io.File

/**
 * [VisionCamera] dedicated to face detection/recognition
 */
interface VisionFaceCamera : VisionCamera {

    /**
     * Configure a liveness strategy to use in this vision camera.
     */
    var liveness: Liveness?

    /**
     * Face proximity feature
     */
    var proximity: Proximity?

    /**
     * Whether or not this face vision camera has a liveness feature installed
     */
    val isLivenessEnabled: Boolean

    /**
     * Whether or not this face vision camera has a proximity feature installed
     */
    val isFaceProximityEnabled: Boolean

    /**
     * Setup this [VisionFaceCamera] with a proper model and options
     */
    fun setup(model: RecognitionModel<Bitmap, Face>)

    /**
     * Capture and perform recognition task on this face vision camera view.
     *
     * @param options control certain aspects of the recognition process.
     * @param onResult callback called once the recognition task is completed.
     */
    fun recognizeStillPicture(options: RecognitionOptions, onResult: (RecognitionResult) -> Unit)

    /**
     * Options to control aspects of the recognition on still pictures.
     *
     * @param saveImage whether we need to save the captured image or not.
     * @param outputDir location where to save the image.
     */
    data class RecognitionOptions(val saveImage: Boolean = false, val outputDir: File? = null)

    /**
     * Result of a recognition tasks.
     *
     * @param file in which the image was saved.
     * @param faces recognized from the image.
     */
    data class RecognitionResult(var file: File? = null, var faces: List<Face> = emptyList())
}