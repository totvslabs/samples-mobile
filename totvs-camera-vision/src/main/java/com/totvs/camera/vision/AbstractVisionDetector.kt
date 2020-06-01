package com.totvs.camera.vision

abstract class AbstractVisionDetector<T : VisionObject>(
    override val key: VisionDetector.Key<*>
) : VisionDetector<T>