package com.totvs.clockin.vision.core

import com.totvs.clockin.vision.face.LivenessEyes
import com.totvs.clockin.vision.face.LivenessFace

/**
 * Exported constants of this module
 */
sealed class ExportableConstant {
    abstract val name: String

    abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            LivenessModes
        )

        operator fun iterator() = all.iterator()

        fun forEach(block: (ExportableConstant) -> Unit) = iterator().forEach(block)
    }
}

/**
 * Liveness modes supported by this library.
 */
object LivenessModes : ExportableConstant() {
    override val name = "LIVENESS_MODE"

    const val FACE = LivenessFace.id
    const val EYES = LivenessEyes.id
    const val NONE = 0 // disabled

    override fun export(): Map<String, Any> = mapOf(
        "FACE" to FACE,
        "EYES" to EYES,
        "NONE" to NONE
    )
}

