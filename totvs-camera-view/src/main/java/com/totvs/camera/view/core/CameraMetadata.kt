package com.totvs.camera.view.core

import androidx.annotation.RestrictTo
import androidx.camera.core.CameraSelector
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.core.CameraFacing

/**
 * Exported constants of this module
 */
sealed class ExportableConstant {
    abstract val name: String

    abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            CameraFacingConstants, CameraZoomLimits
        )

        operator fun iterator() = all.iterator()

        fun forEach(block: (ExportableConstant) -> Unit) = iterator().forEach(block)
    }
}

/**
 * Constants for camera facing. Exported properties annotated with [LensFacing] are expected
 * to assume one of this values. This values map directly to one value of [CameraFacing]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object CameraFacingConstants : ExportableConstant() {
    override val name = "LENS_FACING"

    const val FRONT = CameraSelector.LENS_FACING_FRONT
    const val BACK  = CameraSelector.LENS_FACING_BACK

    override fun export(): Map<String, Any> = mapOf(
        "FRONT" to FRONT,
        "BACK" to BACK
    )
}


/**
 * Constants for limit the zoom values, zoom values must be on [MIN, MAX]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object CameraZoomLimits : ExportableConstant() {
    override val name = "ZOOM_LIMITS"

    const val MIN = 0.0
    const val MAX = 1.0

    override fun export(): Map<String, Any> = mapOf(
        "MAX" to MAX,
        "MIN" to MIN
    )
}