package com.totvs.camera

import androidx.camera.core.CameraSelector

/**
 * Camera lens facing.
 */
public enum class LensFacing : () -> Int {
    /* Conveniently mapping to CameraX len facings */
    FRONT { override fun invoke(): Int = CameraSelector.LENS_FACING_FRONT },
    BACK  { override fun invoke(): Int = CameraSelector.LENS_FACING_BACK  }
}