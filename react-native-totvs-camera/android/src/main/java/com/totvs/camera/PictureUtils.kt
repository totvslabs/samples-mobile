package com.totvs.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

/**
 * Test function to capture pictures and save them in local storage.
 *
 * @author Jansel Valentin
 * @note this function is intended only for show case purpose
 *      once the right approach is determine to handle photos taken
 *      this must be removed
 */
internal fun ImageCapture.testPictureTake(context: Context, executor: Executor, lensFacing: Int, callback: OnPictureTakenCallback) {
    val file = createFile(context)

    val metadata = ImageCapture.Metadata().apply {
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
        .setMetadata(metadata)
        .build()

    takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            Log.e("CameraView", "Photo saved to ${output.savedUri ?: Uri.fromFile(file)}")
            callback(file)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraView", "Photo capture failed ${exception.message}", exception)
        }
    })
}

private fun createFile(context: Context, extension: String = ".jpg") : File {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
}