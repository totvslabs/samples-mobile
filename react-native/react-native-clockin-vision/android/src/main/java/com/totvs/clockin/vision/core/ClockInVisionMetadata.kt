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
            LivenessModes,
            ModelOutputStatus
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

/**
 * Status constants outputted by this library as recognition procedure
 */

object ModelOutputStatus : ExportableConstant() {
    override val name = "MODEL_OUTPUT_STATUS"

    private const val FACE_DETECTED = "FaceDetected"
    private const val FACE_NOT_DETECTED = "FaceNotDetected"
    private const val MULTIPLE_FACES_DETECTED = "MultipleFacesDetected"
    private const val PERSON_NOT_RECOGNIZED = "PersonNotRecognized"

    override fun export(): Map<String, Any> = mapOf(
        "FACE_DETECTED" to FACE_DETECTED,
        "FACE_NOT_DETECTED" to FACE_NOT_DETECTED,
        "MULTIPLE_FACES_DETECTED" to MULTIPLE_FACES_DETECTED,
        "PERSON_NOT_RECOGNIZED" to PERSON_NOT_RECOGNIZED
    )
}

