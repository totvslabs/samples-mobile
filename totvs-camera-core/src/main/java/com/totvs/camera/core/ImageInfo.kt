package com.totvs.camera.core

/** Metadata for an image. */
interface ImageInfo {
    /**
     * Timestamp of the taken image
     */
    val timestamp: Long

    /**
     * Returns the rotation needed to transform the image to the correct orientation.
     *
     * This is a clockwise rotation in degrees that needs to be applied to the image buffer.
     * Note that for images that are in [android.graphics.ImageFormat.JPEG] this value will
     * match the rotation defined in the EXIF.
     *
     * The target rotation is set at the time the image capture was requested.
     */
    val rotationDegrees: Int
}