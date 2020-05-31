package com.totvs.camera.vision.stream

/**
 * Terminal operation that connect to this [VisionStream] and start receiving
 * the [T] that are passing through it.
 *
 * You can use this operator as:
 *
 * someStream.connect { object -> ... do something with the object }
 */
fun <T> VisionStream<T>.connect(block: (T) -> Unit): Connection =
    connect(object : VisionReceiver<T> {
        override fun send(entity: T) {
            block(entity)
        }
    })