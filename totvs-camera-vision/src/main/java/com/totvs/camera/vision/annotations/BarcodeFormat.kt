package com.totvs.camera.vision.annotations

import com.totvs.camera.vision.core.VisionBarcodeFormat

/**
 * Barcode detection types. Possible values are one of the constants defined in
 * [VisionBarcodeFormat]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class BarcodeFormat