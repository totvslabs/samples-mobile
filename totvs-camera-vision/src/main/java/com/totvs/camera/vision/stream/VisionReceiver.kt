package com.totvs.camera.vision.stream

/**
 * This interface models the reception of [T] from an stream.
 */
interface VisionReceiver<T> {
    fun send(entity: T)
}