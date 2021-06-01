package com.totvs.clockin.vision.annotations

import com.totvs.clockin.vision.core.LivenessModes
/**
 * Possible values for properties annotated with this annotation are values of [LivenessModes]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class LivenessMode