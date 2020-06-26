package com.totvs.camera.vision.utils

import android.media.Image
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.barcode.BarcodeDetector
import com.totvs.camera.vision.face.FaceDetector
import com.totvs.camera.vision.face.FastFaceDetector

/**
 * We faced the situation that multiple detectors were using the same underlying [Image]
 * byte buffers of this [ImageProxy] and since they didn't reset the buffers after using it
 * we needed to reset them manually in order to have multiple detection phases over a single
 * [ImageProxy].
 *
 * This method synchronize over this image so that we guarantee that each thread trying to read the
 * underlying [Image] buffers does it when others are not trying to use it to.
 *
 * We recommend that this method is used for such cases in which we need to construct some
 * other representation that after having it, we no longer depends on the state we left
 * the buffers of the image.
 *
 * @see also [FaceDetector], [FastFaceDetector] and [BarcodeDetector]
 */
fun <R> ImageProxy.exclusiveUse(block: () -> R): R = synchronized(this) {
    planes.forEach {
        it.buffer.rewind()
    }
    block()
}