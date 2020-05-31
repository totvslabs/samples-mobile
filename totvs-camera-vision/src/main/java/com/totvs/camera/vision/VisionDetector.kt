package com.totvs.camera.vision

import com.totvs.camera.core.ImageProxy

/**
 * Interface for detectors
 */
interface VisionDetector<T: VisionObject> {
    /**
     * Key of this detector
     */
    val key: Key<*>

    /**
     * Run detection on [image] and report the result back on [onDetected]
     */
    fun detect(image: ImageProxy, onDetected: (T) -> Unit)

    /**
     * Detector key
     */
    interface Key<T : VisionDetector<*>>
}