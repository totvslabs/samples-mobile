package com.totvs.vision.stream

import com.totvs.vision.core.VisionObject
import kotlin.experimental.ExperimentalTypeInference

/**
 * This builder is an schematic construction that exists only for tests. Real streams handle a
 * more complex receiver dispatch.
 *
 * This construct allows fast concept proof and examples as:
 *
 * visionStream {
 *  send(<VisionObject()>)
 *  send(<Another VisionObject()>)
 * }.connect { ... do something with the objects }
 */
@OptIn(ExperimentalTypeInference::class)
fun <T : VisionObject> visionStream(@BuilderInference block: VisionReceiver<T>.() -> Unit): VisionStream<T> =
    BlockStream(block)

/**
 * [Connection] that ignore [disconnect] tokens
 */
private class IgnoreConnection : Connection {
    override fun disconnect() = Unit
}

/**
 * Simple [VisionStream] that ignore [Connection.disconnect] tokens.
 */
private class BlockStream<T : VisionObject>(
    private val block: VisionReceiver<T>.() -> Unit
) : VisionStream<T> {
    override fun connect(receiver: VisionReceiver<T>): Connection =
        IgnoreConnection().also {
            receiver.block()
        }
}