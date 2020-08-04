//
//  OnLiveness.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/3/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

fileprivate let FIELD_EVENT_MODE = "mode"
/**
* Event emitted when liveness feature is enabled and the kind of liveness
* is triggered.
*/
public class OnLiveness : Event {
    
    private let emit: RCTDirectEventBlock?
    
    public required init(emit: RCTDirectEventBlock?) {
        self.emit = emit
    }
    
    public func send(data: LivenessResult) {
        let event:[String: Any] = [
            FIELD_EVENT_MODE: data.mode
        ]
        emit?(event)
    }
}
