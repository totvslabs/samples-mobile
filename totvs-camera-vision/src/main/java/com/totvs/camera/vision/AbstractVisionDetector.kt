package com.totvs.camera.vision

import com.totvs.camera.core.ImageProxy
import java.lang.Exception

abstract class AbstractVisionDetector<T : VisionObject>(
    override val key: VisionDetector.Key<*>
) : VisionDetector<T> {
    // closes a used image
    fun closeImage(image: ImageProxy) = try {
        image.close()
    } catch (ex: Exception) {
    }
}