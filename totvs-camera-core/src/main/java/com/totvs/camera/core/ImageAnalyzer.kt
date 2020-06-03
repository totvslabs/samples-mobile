package com.totvs.camera.core

import android.util.Rational
import android.util.Size

/**
 * Interface to plug analysis on the camera device.
 */
interface ImageAnalyzer {
    /**
     * This method gives the analyzer the ability to request a desired output image size.
     * by default, we request images half of the preview size.
     *
     * This might be used for performance purposes.
     *
     * @param orientationDegrees one of 0, 90, 180, 270
     * @param isPortrait if either device orientation is landscape or portrait
     * @param previewSize size used for previews. This size always respect device orientation
     * and [previewAspectRatio]. i.e its adjusted according to the device orientation to
     * always have [previewAspectRatio].
     * @param previewAspectRatio aspect ratio used  to calculate [previewSize]
     *
     */
    fun desiredOutputImageSize(
        orientationDegrees: Int,
        isPortrait: Boolean,
        previewSize: Size,
        previewAspectRatio: Rational
    ): Size =
        Size(previewSize.width / 2, previewSize.height / 2)

    /**
     * Analyze images received from the camera device. [ImageProxy] coming here are
     * expected to have the same aspect ratio of the [desiredOutputImageSize]. Sometimes
     * the requested size wont be supported by the camera device and in that case the smallest
     * next greater size which posses the same aspect ratio as the [desiredOutputImageSize]
     * will be used instead.
     */
    fun analyze(image: ImageProxy)
}