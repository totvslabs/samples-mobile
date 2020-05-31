package com.totvs.camera.core

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
     */
    fun desiredOutputImageSize(previewSize: Size) : Size =
        Size(previewSize.width / 2, previewSize.height / 2)

    fun analyze(image: ImageProxy)
}