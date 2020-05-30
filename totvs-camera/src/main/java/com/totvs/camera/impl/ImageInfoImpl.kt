package com.totvs.camera.impl

import androidx.annotation.RestrictTo
import com.totvs.camera.core.ImageInfo

/**
 * Concrete implementation of [ImageInfo]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ImageInfoImpl(
    override val timestamp: Long,
    override val rotationDegrees: Int
) : ImageInfo