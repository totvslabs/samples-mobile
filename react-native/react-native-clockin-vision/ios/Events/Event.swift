//
//  Event.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/3/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* React Native JS event representation.
*
* Events emitted to the JS side are advised to implement this interface
*
*/
public protocol Event {
    associatedtype Data
    
    init(emit: RCTDirectEventBlock?)
    
    func send(data: Data)
}
