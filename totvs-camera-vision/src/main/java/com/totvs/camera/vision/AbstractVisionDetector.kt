package com.totvs.camera.vision

/**
 * Base vision detector
 */
abstract class AbstractVisionDetector<T : VisionObject>(
    override val key: VisionDetector.Key<*>
) : VisionDetector<T>