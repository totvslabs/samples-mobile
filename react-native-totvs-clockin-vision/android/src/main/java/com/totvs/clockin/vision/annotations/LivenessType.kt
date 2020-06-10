package com.totvs.clockin.vision.annotations

import com.totvs.clockin.vision.face.LivenessMode
/**
 * Possible values for properties annotated with this annotation are values of [LivenessMode]
 * or it ordinals
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class LivenessType