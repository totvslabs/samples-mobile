//
//  VisionStream.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

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
*/
open class VisionStream<T> {
    /**
     * Connect to this stream a [VisionReceiver] that manipulate the incoming [T]
     * and returns a [Connection] that the caller can use to stop receiving objects from this
     * stream.
     */
    open func connect(_ receiver: VisionReceiver<T>) -> Connection { return IgnoreConnection() }
}
