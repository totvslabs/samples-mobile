package com.totvs.clockin.vision.utils

import android.content.Context
import androidx.annotation.RestrictTo
import com.totvs.camera.core.OutputFileOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create a random file in the provided [outputDirectory] with [extension]. if [outputDirectory]
 * if not provided the created file is located in the data directory of the app.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun createFile(context: Context, outputDirectory: File? = null, extension: String = "jpg") : File {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    val parent = outputDirectory ?: context.filesDir
    return File(parent, "IMG_${sdf.format(Date())}.$extension")
}
