package com.totvs.vision.stream

import com.totvs.vision.core.VisionObject

/**
 * This interface models the reception of [VisionObject] from an stream.
 */
interface VisionReceiver<T : VisionObject> {
    fun send(entity: T)
}