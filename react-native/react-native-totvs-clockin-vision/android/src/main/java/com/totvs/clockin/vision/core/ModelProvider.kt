package com.totvs.clockin.vision.core

import android.graphics.Bitmap
import com.totvs.clockin.vision.face.Face
import com.totvs.clockin.vision.internal.NativeFaceModel
import com.totvs.clockin.vision.core.Model.Config

/**
 * Model provider for all the models that this library uses.
 */
object ModelProvider {
    /**
     * Returns a face recognition model
     */
    fun getFaceRecognitionModel(config: Config): RecognitionModel<Bitmap, Face> =
        NativeFaceModel.getInstance(config)

    /**
     * Returns a face detection model
     */
    fun getFaceDetectionModel(config: Config): DetectionModel<Bitmap, Face> =
        NativeFaceModel.getInstance(config)
}