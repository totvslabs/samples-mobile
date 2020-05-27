package com.totvs.camera.core

import android.graphics.Rect
import android.media.Image
import java.nio.ByteBuffer

/**
 * A public proxy with the same interface as [Image].
 *
 * This allow us to offer a unified and provided interface of an image
 * that is independent of the provider of captured images.
 */
public interface ImageProxy : AutoCloseable {
    /**
     * Close this resource. This will close the underlying [image] field
     */
    override fun close()

    /**
     * raw image for a single capture.
     *
     * Since this is a proxy around an image, is possible that over time
     * we wrap here another representation of image, hence this might return null
     * under those conditions.
     *
     * Notice that [image] must not be closed by the caller, instead close this proxy.
     */
    val image: Image?

    /**
     * Get/Set the crop rectangle.
     *
     * @see [android.media.Image.getCropRect]
     */
    var cropRect: Rect

    /**
     * Returns the image format.
     *
     * @see [android.media.Image.getFormat()]
     */
    val format: Int

    /**
     * Returns the image width.
     *
     * @see []android.media.Image.getWidth()]
     */
    val width: Int

    /**
     * Returns the image height.
     *
     * @see [android.media.Image.getHeight()]
     */
    val height: Int

    /**
     * Returns the info of this image
     */
    val imageInfo: ImageInfo

    /**
     * Returns the array of planes
     */
    val planes: List<PlaneProxy>

    /**
     * A plane proxy which act as a subset interface of [android.media.Image.Plane]
     */
    interface PlaneProxy {
        /**
         * Returns the pixel of data
         */
        val buffer: ByteBuffer
    }
}