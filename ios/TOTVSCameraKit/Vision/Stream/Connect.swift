//
//  Connect.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

public extension VisionStream {
    func connect(_ block: @escaping (T) -> Void) -> Connection {
        return connect(BlockReceiver(block: block))
    }
}
