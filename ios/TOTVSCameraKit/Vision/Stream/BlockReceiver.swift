//
//  BlockReceiver.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
 This is a special kind of receiver to be used as inlined implementations of
 [VisionReceiver]. This is meant to be used as equivalent to `object : VisionReceiver<T> { ... }`
 in kotlin
 */
class BlockReceiver<T> : VisionReceiver<T> {
    let block: (T) -> Void
    init(block: @escaping (T) -> Void) {
        self.block = block
    }
    
    override func send(value: T) {
        block(value)
    }
}
