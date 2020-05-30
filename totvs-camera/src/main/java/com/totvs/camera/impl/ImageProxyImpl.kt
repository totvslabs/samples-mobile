package com.totvs.camera.impl

import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import androidx.annotation.RestrictTo
import com.totvs.camera.core.ImageInfo
import com.totvs.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * Concrete implementation of [ImageProxy]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ImageProxyImpl(
    override val image: Image?,
    private  val rotationDegrees: Int
) : ImageProxy {

    override var cropRect: Rect
        get() = image?.cropRect ?: Rect(0, 0, 0, 0)
        set(value) {
            image?.cropRect = value
        }

    override val format: Int
        get() = image?.format ?: ImageFormat.UNKNOWN

    override val width: Int
        get() = image?.width ?: 0

    override val height: Int
        get() = image?.height ?: 0

    override val imageInfo: ImageInfo
        get() = ImageInfoImpl(
            timestamp = image?.timestamp ?: 0,
            rotationDegrees = rotationDegrees
        )

    override val planes: List<ImageProxy.PlaneProxy>
        get() = (image?.planes ?: emptyArray()).map {
            PlaneProxyImpl(buffer = it.buffer)
        }

    override fun close() {
        image?.close()
    }

    private class PlaneProxyImpl(override val buffer: ByteBuffer) : ImageProxy.PlaneProxy
}