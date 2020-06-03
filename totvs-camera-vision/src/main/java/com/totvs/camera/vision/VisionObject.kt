package com.totvs.camera.vision

import android.graphics.RectF
import android.util.Size

/**
 * [VisionObject] conceptually comes from an image as source. Source properties here
 * carries information about the source image such as size and rotation needed to be
 * applied to the image to have it upside right.
 *
 * These properties depends on the producer of the images and object.
 */
abstract class VisionObject {
    /**
     * Source image size where this [VisionObject] was found
     */
    abstract val sourceSize: Size

    /**
     * Bounding box of this [VisionObject]. This value is based on the [sourceSize] coordinate.
     */
    abstract val boundingBox: RectF?

    /**
     * Source image rotation where this [VisionObject] was found. Expected values are
     * 0, 90, 180, 270
     */
    abstract val sourceRotationDegrees: Int
}