package com.totvs.camera.core.annotations

import androidx.annotation.RestrictTo

/**
 * We annotate with this annotation, those sections that need special care for potential
 * performance issues. We need to come back to this again and remove the annotation before release.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION
)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
annotation class NeedsProfiling(val what: String = "")