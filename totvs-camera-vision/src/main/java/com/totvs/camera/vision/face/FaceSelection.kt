package com.totvs.camera.vision.face

import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.totvs.camera.vision.core.SelectionStrategy

/**
 * Selection strategy for detected face
 */
enum class FaceSelection : SelectionStrategy<FirebaseVisionFace> {
    FIRST {
        override fun invoke(
            barcodes: List<FirebaseVisionFace>
        ): FirebaseVisionFace = barcodes.first()
    },
    MOST_PROMINENT { // @TODO what is most prominent
        override fun invoke(
            barcodes: List<FirebaseVisionFace>
        ): FirebaseVisionFace = barcodes.first()
    }
}