package com.totvs.camera.vision.stream

/**
 * A [Transformer] tries to mimic the same behavior as [transform] _Intermediate operator_
 * but giving the ability to have classes implementing a transformation strategy.
 *
 * This interface is convenient when the operation that would otherwise fit in the [transform]
 * block would needs to be stateful or is more convenient to have it on a separate class.
 */
interface Transformer<T, R> {
    /**
     * Receives [value] upstream value and must emit values to
     * downstream receiver
     */
    fun transform(value: T, receiver: VisionReceiver<R>)
}