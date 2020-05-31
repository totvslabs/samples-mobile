package com.totvs.camera.view

import androidx.camera.core.CameraSelector
import com.totvs.camera.core.CameraFacing

/**
 * Map a [CameraFacing] to a [CameraSelector.LensFacing]
 */
internal val CameraFacing.toCameraXFacing
    get() = if (this == CameraFacing.FRONT) {
        CameraSelector.LENS_FACING_FRONT
    } else {
        CameraSelector.LENS_FACING_BACK
    }

/**
 * Map from a [CameraSelector.LensFacing] to a [CameraFacing]
 */
internal val Int.fromCameraXFacing
    get() = if (this == CameraSelector.LENS_FACING_FRONT) {
        CameraFacing.FRONT
    } else {
        CameraFacing.BACK
    }