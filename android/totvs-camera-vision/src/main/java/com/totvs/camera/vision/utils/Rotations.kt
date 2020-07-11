package com.totvs.camera.vision.utils

import android.view.Surface
import androidx.annotation.RestrictTo
import com.google.android.gms.vision.Frame

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Int.toImageFrameRotation() = when (this) {
    0,   Surface.ROTATION_0 -> Frame.ROTATION_0
    90,  Surface.ROTATION_90 -> Frame.ROTATION_90
    180, Surface.ROTATION_180 -> Frame.ROTATION_180
    270, Surface.ROTATION_270 -> Frame.ROTATION_270
    else -> throw IllegalArgumentException("Rotation must be 0, 90, 180, 270")
}