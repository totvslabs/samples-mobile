package com.totvs.camera.core

import java.io.File

/**
 * Callback to indicate when the image has been saved
 */
typealias OnImageSaved = (file: File?, throwable: Throwable?) -> Unit

/**
 * Callback to indicate when an image was captured.
 */
typealias OnImageCaptured = (image: ImageProxy?, throwable: Throwable?) -> Unit