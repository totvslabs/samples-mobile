package com.totvs.camera.vision.core

/**
 * Exported constants of this module
 */
sealed class ExportableConstant {
    abstract val name: String

    abstract fun export(): Map<String, Any>

    companion object {
        private val all = listOf(
            VisionBarcodeFormat
        )

        operator fun iterator() = all.iterator()

        inline fun forEach(block: (ExportableConstant) -> Unit) = iterator().forEach(block)
    }
}

// as reference: here's a visual correspondence with the types and images:
// https://medium.com/google-developer-experts/exploring-firebase-mlkit-on-android-barcode-scanning-part-three-cc6f5921a108
object VisionBarcodeFormat : ExportableConstant() {
    override val name = "BARCODE_FORMAT"

    const val QR_CODE = 1
    const val AZTEC = 2

    override fun export(): Map<String, Any> = mapOf(
        "QR_CODE" to QR_CODE,
        "AZTEC" to AZTEC
    )
}