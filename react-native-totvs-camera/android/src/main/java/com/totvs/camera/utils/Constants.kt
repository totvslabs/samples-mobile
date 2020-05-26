package com.totvs.camera.utils

import androidx.camera.core.CameraSelector

/**
 * Exportable constants
 */
internal sealed class ExportableConstants {
    abstract val name: String

    abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            CameraFacing, ZoomLimits
        )

        operator fun iterator() = all.iterator()

        fun forEach(block: (ExportableConstants) -> Unit) = iterator().forEach(block)
    }
}

/**
 * Constants for camera facing
 */
internal object CameraFacing : ExportableConstants() {
    override val name = "LENS_FACING"

    override fun export(): Map<String, Any> = mapOf(
        "FRONT" to Constants.CAMERA_FACING_FRONT,
        "BACK" to Constants.CAMERA_FACING_BACK
    )
}

/**
 * Constants for limit the zoom values, zoom values must be on [MIN, MAX]
 */
internal object ZoomLimits : ExportableConstants() {
    override val name = "ZOOM_LIMITS"

    override fun export(): Map<String, Any> = mapOf(
        "MAX" to Constants.ZOOM_MAX,
        "MIN" to Constants.ZOOM_MIN
    )
}


/**
 * General unnamed constants
 */
internal object Constants {
    // camera lens facing
    const val CAMERA_FACING_FRONT = CameraSelector.LENS_FACING_FRONT
    const val CAMERA_FACING_BACK = CameraSelector.LENS_FACING_BACK

    // camera zoom limit values
    const val ZOOM_MIN = 0.0
    const val ZOOM_MAX = 1.0
}