package com.totvs.camera.view

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.annotation.experimental.UseExperimental
import androidx.camera.core.*
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.core.OnImageCaptured
import com.totvs.camera.core.OnImageSaved
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.view.core.ImageProxyImpl
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

/**
 * Test function to capture pictures and save them in local storage.
 *
 * @note this function is intended only for show case purpose
 *      once the right approach is determine to handle photos taken
 *      this must be removed
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ImageCapture.testPictureTake(
    context: Context,
    executor: Executor,
    lensFacing: CameraFacing,
    options: OutputFileOptions,
    onSaved: OnImageSaved
) {
    val file = createFile(context)

    val metadata = ImageCapture.Metadata().apply {
        isReversedHorizontal = lensFacing.toFacingConstant == CameraSelector.LENS_FACING_FRONT
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
        .setMetadata(metadata)
        .build()

    takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            Log.e("CameraView", "Photo saved to ${output.savedUri ?: Uri.fromFile(file)}")
            onSaved(file, null)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraView", "Photo capture failed ${exception.message}", exception)
            onSaved(null, exception)
        }
    })
}

@UseExperimental(markerClass = ExperimentalGetImage::class)
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ImageCapture.testPictureTake(
    executor: Executor,
    onCaptured: OnImageCaptured
) {
    takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            onCaptured(ImageProxyImpl(image.image, image.imageInfo.rotationDegrees), null)
        }

        override fun onError(exception: ImageCaptureException) {
            onCaptured(null, exception)
        }
    })
}

private fun createFile(context: Context, extension: String = ".jpg") : File {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
}