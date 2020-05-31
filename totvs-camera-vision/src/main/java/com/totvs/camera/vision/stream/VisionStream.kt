package com.totvs.camera.vision.stream

import com.totvs.camera.vision.VisionObject
import com.totvs.camera.vision.NullVisionObject

/**
 * [VisionStream] model a stream of vision object passing through a processing pipe
 * that can transform the object as it goes until it reaches the end of the pipe.
 *
 * The thread safety of a [VisionStream] is an implementation detail.
 *
 * The nature of the stream, if either is a hot stream or a cold stream is
 * also an implementation detail.
 *
 * Operations on an stream are categorized into _Intermediate operators_ such as [filter]
 * [map], etc, and _Terminal operators_ as [connect]. _Intermediate operators_ don't connect
 * to upstream when they are created but instead when the caller connect to it. This mean
 * that we can chain as many _Intermediate operators_ as we want without actually connecting
 * to the upstream stream, but once the caller decides to connect with [connect], then
 * the chain of transformation by the operators connect to the upstream.
 *
 * [VisionStream] doesn't work on null values, in order to represent an absence of value
 * use [NullVisionObject]
 *
 * As side note, we could make this interface along with the companions, a generic one
 * by not restricting it use to only [VisionObject] but since the purpose of this
 * library is to handle such objects we impose that constraint into the types.
 */
interface VisionStream<T : VisionObject> {
    /**
     * Connect to this stream a [VisionReceiver] that manipulate the incoming [VisionObject]
     * and returns a [Connection] that the caller can use to stop receiving objects from this
     * stream.
     */
    fun connect(receiver: VisionReceiver<T>) : Connection
}