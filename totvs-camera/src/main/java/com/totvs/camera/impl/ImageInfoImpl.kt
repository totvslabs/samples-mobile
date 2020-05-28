package com.totvs.camera.impl

import com.totvs.camera.core.ImageInfo

internal class ImageInfoImpl(
    override val timestamp: Long,
    override val rotationDegrees: Int
) : ImageInfo