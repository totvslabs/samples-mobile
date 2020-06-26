package com.totvs.camera.view

import com.totvs.camera.core.CameraFacing
import com.totvs.camera.view.core.CameraFacingConstants

/**
 * Map a [CameraFacing] to a [CameraFacingConstants]
 */
val CameraFacing.toFacingConstant
    get() = if (this == CameraFacing.FRONT) {
        CameraFacingConstants.FRONT
    } else {
        CameraFacingConstants.BACK
    }

/**
 * Map from a [CameraFacingConstants] to a [CameraFacing]
 */
val Int.toCameraFacing
    get() = if (this == CameraFacingConstants.FRONT) {
        CameraFacing.FRONT
    } else {
        CameraFacing.BACK
    }