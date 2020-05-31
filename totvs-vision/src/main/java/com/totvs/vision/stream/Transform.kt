package com.totvs.vision.stream

import com.totvs.vision.core.VisionObject
import kotlin.experimental.ExperimentalTypeInference

/**
 * [Connection] class that delegate all connection operations to the _delegated_ connection.
 *
 * We use this as a trampoline to the connection we perform on [transform] operator.
 * This allows us to preserve the original connection source and gracefully disconnect
 * from upstream when the caller invoke [Connection.disconnect] on the transformed
 * [VisionStream]
 */
private class DelegatedConnection(delegated: Connection) : Connection by delegated

/**
 * This is the most general _Intermediate operator_. It transform an upstream into another
 * stream.
 */
@OptIn(ExperimentalTypeInference::class)
fun <T : VisionObject, R : VisionObject> VisionStream<T>.transform(
    @BuilderInference transform: VisionReceiver<R>.(value: T) -> Unit
): VisionStream<R> = object : VisionStream<R> {
    override fun connect(receiver: VisionReceiver<R>): Connection {
        val connection = this@transform
            .connect { value ->
                receiver.transform(value)
            }
        return DelegatedConnection(connection)
    }
}

/**
 * This is the most general _Intermediate operator_. It transform an upstream into another
 * stream using a [Transformer] interface.
 */
fun <T : VisionObject, R : VisionObject> VisionStream<T>.transform(
    transformer: Transformer<T, R>
): VisionStream<R> = object : VisionStream<R> {
    override fun connect(receiver: VisionReceiver<R>): Connection {
        val connection = this@transform
            .connect { value ->
                transformer.transform(value, receiver)
            }
        return DelegatedConnection(connection)
    }
}

/**
 * _Intermediate operator_ that filter the elements according to [predicate]
 */
fun <T: VisionObject> VisionStream<T>.filter(predicate: (T) -> Boolean) : VisionStream<T> =
    transform { value ->
        if (predicate(value)) send(value)
    }

/**
 * _Intermediate operator_ that filter the elements that are instance of [T]
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : VisionObject> VisionStream<*>.filterIsInstance() : VisionStream<T> =
    filter { it is T } as VisionStream<T>
