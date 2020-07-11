//
//  PluggedStream.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
 This interface allows us to connect to an upstream stream and allow a downstream block
 act accordingly on the upstream, returning the connection the downstream block
 returns.
 
 This shows to be helpful as equivalent of the pattern `object : VisionStream<T> { override connect(..) .. }`
 on kotlin.
 */
class PluggedStream<T> : VisionStream<T> {
    let downstream: (VisionReceiver<T>) -> Connection
    
    init(downstream: @escaping (VisionReceiver<T>) -> Connection) {
        self.downstream = downstream
    }
    
    override func connect(receiver: VisionReceiver<T>) -> Connection {
        return downstream(receiver)
    }
}
