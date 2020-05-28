package com.totvs.camera.core

import com.totvs.camera.utils.Constants

/**
 * Camera lens facing.
 */
public enum class LensFacing : () -> Int {
    /* Conveniently mapping to constant facings */
    FRONT { override fun invoke(): Int = Constants.CAMERA_FACING_FRONT },
    BACK  { override fun invoke(): Int = Constants.CAMERA_FACING_BACK  }
}
