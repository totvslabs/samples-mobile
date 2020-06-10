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
 * Capture an image and save it into the output file specified on [options] or a
 * random one is created. Images are saved in JPEG format.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ImageCapture.internalTakePicture(
    context: Context,
    executor: Executor,
    options: OutputFileOptions,
    onSaved: OnImageSaved
) {
    val file = options.file ?: createFile(context)

    val metadata = ImageCapture.Metadata().apply {
        isReversedHorizontal = options.isReversedHorizontal
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
        .setMetadata(metadata)
        .build()

    takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            onSaved(file, null)
        }

        override fun onError(exception: ImageCaptureException) {
            onSaved(null, exception)
        }
    })
}

/**
 * Capture an image and deliver it to the caller.
 */
@UseExperimental(markerClass = ExperimentalGetImage::class)
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ImageCapture.internalTakePicture(
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

/**
 * Create a random file. This is use in case no file is specified in [OutputFileOptions]
 * on [ImageCapture.takePicture]
 */
private fun createFile(context: Context, extension: String = ".jpg") : File {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
}