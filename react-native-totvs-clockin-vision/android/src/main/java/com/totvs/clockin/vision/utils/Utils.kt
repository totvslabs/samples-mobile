package com.totvs.clockin.vision.utils

import android.util.Base64
import android.util.Log
import androidx.annotation.RestrictTo
import com.totvs.clockin.vision.core.ClockInVisionModuleOptions
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

/**
 * Get the by representation of the image in [path] if possible, null otherwise
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getFileImageData(path: String): ByteArray? = try {
    val file = File(path)
    val bytes = ByteArray(file.length().toInt())
    BufferedInputStream(FileInputStream(file)).use { buffer ->
        buffer.read(bytes)
    }
    bytes
} catch (ex: Exception) {
    if (ClockInVisionModuleOptions.DEBUG_ENABLED) {
        Log.e("getFileImageData", "error reading image file at $path", ex)
    }
    null
}

/**
 * Get a base64 representation out of a [ByteArray] if possible, null otherwise
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ByteArray.toBase64(): String? = if (size == 0) null else Base64.encodeToString(
    this,
    Base64.DEFAULT
)

/**
 * Get the base64 representation of an image if possible, null otherwise
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getFileImageBase64(path: String): String? = getFileImageData(path)
    ?.let {
        it.toBase64()
    }


/**
 * Delete a file from the system, returns a boolean indicating the result of the operation.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun deleteFile(path: String): Boolean = try {
    val file = File(path)
    if (file.exists()) {
        file.delete()
    } else {
        false
    }
} catch (ex: Exception) {
    if (ClockInVisionModuleOptions.DEBUG_ENABLED) {
        Log.e("deleteFile", "error deleting file at $path", ex)
    }
    false
}