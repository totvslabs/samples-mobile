package com.totvs.camera

import java.io.File

/**
 * Callback to be called when picture are taken from the camera device
 */
typealias OnPictureTakenCallback = (file: File) -> Unit