package com.totvs.camera.core

import androidx.annotation.RestrictTo
import androidx.camera.core.CameraSelector

/**
 * Exportable constants
 */
sealed class ExportableConstant {
    internal abstract val name: String

    internal abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            CameraFacing, ZoomLimits
        )

        internal operator fun iterator() = all.iterator()

        internal fun forEach(block: (ExportableConstant) -> Unit) = iterator().forEach(block)
    }
}

/**
 * Constants for camera facing
 */
object CameraFacing : ExportableConstant() {
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
internal object ZoomLimits : ExportableConstant() {
    override val name = "ZOOM_LIMITS"

    const val MIN = 0.0
    const val MAX = 1.0

    override fun export(): Map<String, Any> = mapOf(
        "MAX" to MAX,
        "MIN" to MIN
    )
}