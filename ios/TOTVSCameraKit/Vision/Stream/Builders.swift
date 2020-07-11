//
//  Builders.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
* This builder is an schematic construction that exists only for tests. Real streams handle a
* more complex receiver dispatch.
*
* This construct allows fast concept proof and examples as:
*
* visionStream<Int>({ r in
*  r.send(<VisionObject()>)
*  r.send(<Another VisionObject()>)
* }).connect { ... do something with the objects }
*/
public func visionStream<T>(_ block: @escaping (VisionReceiver<T>) -> Void) -> VisionStream<T> {
    return BlockStream(block: block)
}

/**
* Simple [VisionStream] that ignore [Connection.disconnect] tokens.
*/
fileprivate class BlockStream<T> : VisionStream<T> {
    let block: (VisionReceiver<T>) -> Void
    
    init(block: @escaping (VisionReceiver<T>) -> Void) {
        self.block = block
    }
    
    override func connect(receiver: VisionReceiver<T>) -> Connection {
        block(receiver)
        return IgnoreConnection()
    }
}
