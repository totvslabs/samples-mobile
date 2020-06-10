package com.totvs.clockin.vision.face

import android.graphics.Bitmap
import com.totvs.clockin.vision.core.RecognitionModel
import com.totvs.clockin.vision.core.VisionCamera

/**
 * [VisionCamera] dedicated to face detection/recognition
 */
interface FaceVisionCamera : VisionCamera {

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
     * Setup this [FaceVisionCamera] with a proper model and options
     */
    fun setup(model: RecognitionModel<Bitmap, Face>, options: FaceVisionOptions)

    /**
     * Capture and perform recognition task on this face vision camera view.
     */
    fun recognizeStillPicture()
}