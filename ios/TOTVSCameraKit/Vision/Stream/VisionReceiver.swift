//
//  VisionReceiver.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

/**
 * This interface models the reception of [T] from an stream.
 *
 * @discussion not made protocol, because of the underlyming associated type and
 * it'll conflict with our need to use this class as parameters and during design
 * it was a little bit hard to express the type-safety with protocols and associated
 * types without the need to have intermediaty conforming claasses.
 */
open class VisionReceiver<T> {
    public init() { }
    open func send(value: T) { }
}
