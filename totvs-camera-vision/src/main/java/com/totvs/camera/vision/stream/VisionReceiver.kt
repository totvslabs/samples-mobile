package com.totvs.camera.vision.stream

import com.totvs.camera.vision.VisionObject

/**
 * This interface models the reception of [VisionObject] from an stream.
 */
interface VisionReceiver<T : VisionObject> {
    fun send(entity: T)
}