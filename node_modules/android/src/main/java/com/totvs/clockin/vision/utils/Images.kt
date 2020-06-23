package com.totvs.clockin.vision.utils

import android.graphics.*
import android.media.Image
import androidx.annotation.RestrictTo
import java.io.ByteArrayOutputStream

/**
 * Convert an [Image] into a [Bitmap].
 *
 * This function consider if the image to be converted into [Bitmap]
 * is on JPEG format. If that's the case then a fast path is taken.
 *
 * This utility function is mainly used for images that comes from
 * camera captures, which comes in JPEG format.
 *
 * @param rotationDegrees indicate the degrees we need to rotate the bitmap
 * to produce a valid normal oriented image bitmap.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Image.toBitmap(rotationDegrees: Int): Bitmap {

    if (format == ImageFormat.JPEG) {
        if (null != planes && planes!!.size == 1) {
            val buffer = planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return if (rotationDegrees == 0)
                bitmap
            else
                bitmap.rotate(rotationDegrees)
        } else {
            throw IllegalArgumentException("Unexpected image format, JPEG should have exactly 1 image plane")
        }
    } else {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return if (rotationDegrees == 0)
            bitmap
        else
            bitmap.rotate(rotationDegrees)
    }
}

private fun Bitmap.rotate(degrees: Int) : Bitmap {
    if (degrees != 0 && degrees != 90 && degrees != 180 && degrees != 270) {
        throw IllegalArgumentException("Valid degrees rotations are 0, 90, 180, 270")
    }
    if (degrees == 0) {
        return this;
    }
    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}