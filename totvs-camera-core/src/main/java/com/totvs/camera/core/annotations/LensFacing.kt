package com.totvs.camera.core.annotations

import com.totvs.camera.core.CameraFacing

/**
 * Possible values for properties annotated with this annotation are:
 * [CameraFacing.BACK] and [CameraFacing.FRONT]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class LensFacing