package com.totvs.camera.vision.utils

import android.view.Surface
import androidx.annotation.RestrictTo
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Int.toFirebaseVisionRotation() = when (this) {
    0,   Surface.ROTATION_0 -> FirebaseVisionImageMetadata.ROTATION_0
    90,  Surface.ROTATION_90 -> FirebaseVisionImageMetadata.ROTATION_90
    180, Surface.ROTATION_180 -> FirebaseVisionImageMetadata.ROTATION_180
    270, Surface.ROTATION_270 -> FirebaseVisionImageMetadata.ROTATION_270
    else -> throw IllegalArgumentException("Rotation must be 0, 90, 180, 270")
}