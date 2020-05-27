package com.totvs.camera.core

import java.io.File

/**
 * Output options for saving the captured images
 */
data class OutputFileOptions(
    /** output file for a taken picture */
    val file: File,
    /**
     * Indicate whether the saved taken image is reversed horizontally.
     *
     * This can happens when we're using a front camera. By default front cameras
     * have an horizontal mirroring regarding the back camera. We need to set this
     * in order to let the saving engine knows that the image needs to be treated accordingly
     */
    val isReversedHorizontal: Boolean,
    /**
     * * Indicate whether the saved taken image is reversed vertically.
     */
    val isReversedVertical: Boolean
)