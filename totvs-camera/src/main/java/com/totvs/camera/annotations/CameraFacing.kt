package com.totvs.camera.annotations

import com.totvs.camera.utils.Constants

/**
 * Possible values for properties annotated with this annotation are:
 * [Constants.CAMERA_FACING_BACK] and [Constants.CAMERA_FACING_FRONT]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class CameraFacing