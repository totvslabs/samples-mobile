package com.totvs.camera.utils

import androidx.annotation.RestrictTo
import androidx.camera.core.CameraSelector

/**
 * Exportable constants
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal sealed class ExportableConstant {
    abstract val name: String

    abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            CameraFacing, ZoomLimits
        )

        operator fun iterator() = all.iterator()

        fun forEach(block: (ExportableConstant) -> Unit) = iterator().forEach(block)
    }
}

/**
 * Constants for camera facing
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object CameraFacing : ExportableConstant() {
    override val name = "LENS_FACING"

    const val FRONT = CameraSelector.LENS_FACING_FRONT
    const val BACK = CameraSelector.LENS_FACING_BACK

    override fun export(): Map<String, Any> = mapOf(
        "FRONT" to FRONT,
        "BACK" to BACK
    )
}


/**
 * Constants for limit the zoom values, zoom values must be on [MIN, MAX]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object ZoomLimits : ExportableConstant() {
    override val name = "ZOOM_LIMITS"

    const val MIN = 0.0
    const val MAX = 1.0

    override fun export(): Map<String, Any> = mapOf(
        "MAX" to MAX,
        "MIN" to MIN
    )
}