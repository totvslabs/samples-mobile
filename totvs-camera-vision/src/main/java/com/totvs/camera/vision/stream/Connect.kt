package com.totvs.camera.vision.stream

import com.totvs.camera.vision.VisionObject

/**
 * Terminal operation that connect to this [VisionStream] and start receiving
 * the [VisionObject] that are passing through it.
 *
 * You can use this operator as:
 *
 * someStream.connect { object -> ... do something with the object }
 */
fun <T : VisionObject> VisionStream<T>.connect(block: (T) -> Unit): Connection =
    connect(object : VisionReceiver<T> {
        override fun send(entity: T) {
            block(entity)
        }
    })