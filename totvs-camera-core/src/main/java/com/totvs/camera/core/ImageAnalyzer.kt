package com.totvs.camera.core

/**
 * Interface to plug analysis on the camera device.
 */
interface ImageAnalyzer {
    fun analyze(image: ImageProxy)
}