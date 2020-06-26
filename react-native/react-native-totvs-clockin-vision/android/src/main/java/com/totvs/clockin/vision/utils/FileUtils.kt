package com.totvs.clockin.vision.utils

import android.content.Context
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.annotation.RawRes
import androidx.annotation.RestrictTo
import com.totvs.clockin.vision.R
import com.totvs.clockin.vision.core.ClockInVisionModuleOptions
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread

/**
 * Constants for the clock-in module
 */
internal object Constants {
    const val DEFAULT_MODEL_OUTPUT_DIR_NAME = "carol_offline_face_recognition"
    const val CAPTURES_OUTPUT_DIR_NAME = "images"
    const val PENDING_EMPLOYEES_OUTPUT_DIR_NAME = "pendingEmployeeImages"
    const val MODEL_SHAPE_DIR_NAME = "shape_predictor_5_face_landmarks.dat"
    const val MODEL_DESCRIPTOR_DIR_NAME = "dlib_face_recognition_resnet_model_v1.dat"
    const val NO_MEDIA = ".nomedia"
}

/**
 * Utility class to save transient state through utility changes
 */
private object TransientState {
    // the requested and desired model output directory name
    var modelOutputDirName: String? = null
}


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
internal fun getFileBase64(path: String): String? = getFileImageData(path)
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

/**
 * Copy a file from the raw folder to an specified file
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun copyRawFileOver(context: Context, @RawRes raw: Int, output: File) {
    try {
        context.resources.openRawResource(raw).use { input ->
            FileOutputStream(output).use { out ->
                val buffer = ByteArray(1024)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    out.write(buffer, 0, read)
                }
            }
        }
    } catch (ex: Exception) {
        if (ClockInVisionModuleOptions.DEBUG_ENABLED) {
            Log.e("copyRawFileOver", "error copying raw file", ex)
        }
    }
}

/**
 * Set the model output directory name
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun setModelDirName(name: String) {
    TransientState.modelOutputDirName = name
}

/**
 * Returns the model output directory path
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getModelOutputDir(): String {
    var dirName = if (TransientState.modelOutputDirName.isNullOrEmpty())
        Constants.DEFAULT_MODEL_OUTPUT_DIR_NAME
    else
        TransientState.modelOutputDirName

    val sdcard = Environment.getExternalStorageDirectory()
    return "${sdcard.absolutePath}${File.separator}$dirName"
}

/**
 * Returns the captures output directory path
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getCapturesDirectory() =
    "${getModelOutputDir()}${File.separator}${Constants.CAPTURES_OUTPUT_DIR_NAME}"

/**
 * Returns the pending employees images output directory path
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getPendingEmployeesDirectory() =
    "${getModelOutputDir()}${File.separator}${Constants.PENDING_EMPLOYEES_OUTPUT_DIR_NAME}"

/**
 * Returns the model shape output directory
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getModelShapesDirectory() =
    "${getModelOutputDir()}${File.separator}${Constants.MODEL_SHAPE_DIR_NAME}"

/**
 * Returns the model descriptors output directory
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getModelDescriptorsDirectory() =
    "${getModelOutputDir()}${File.separator}${Constants.MODEL_DESCRIPTOR_DIR_NAME}"


/**
 * Returns the .no_media directory path.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getNoMediaDirectory() =
    "${getModelOutputDir()}${File.separator}${Constants.NO_MEDIA}"

/**
 * Setup the model output directories
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun prepareModelDirectories(context: Context, onDone: () -> Unit) {
    thread {
        try {
            val model = File(getModelOutputDir())
            val media = File(getNoMediaDirectory())
            val images = File(getCapturesDirectory())
            val pending = File(getPendingEmployeesDirectory())
            val shapes = File(getModelShapesDirectory())
            val descriptors = File(getModelDescriptorsDirectory())

            model.mkdirs()
            media.mkdirs()
            images.mkdirs()
            pending.mkdirs()
            // If the file does not exist copy it over
            if (!shapes.exists()) {
                copyRawFileOver(context, R.raw.shape_predictor_5_face_landmarks, shapes)
            }
            if (!descriptors.exists()) {
                copyRawFileOver(context, R.raw.dlib_face_recognition_resnet_model_v1, descriptors)
            }
        } catch (ex: Exception) {
            if (ClockInVisionModuleOptions.DEBUG_ENABLED) {
                Log.e("prepareModelDirectories", "error configuring model directories")
            }
        }
        onDone()
    }
}

