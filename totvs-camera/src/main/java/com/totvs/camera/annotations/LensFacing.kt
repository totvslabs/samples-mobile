package com.totvs.camera.annotations

import com.totvs.camera.utils.CameraFacing

/**
 * Possible values for properties annotated with this annotation are:
 * [CameraFacing.BACK] and [CameraFacing.FRONT]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class LensFacing