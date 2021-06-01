//
//  OnFaceProximity.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/3/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

fileprivate let FIELD_IS_UNDER_THRESHOLD = "isUnderThreshold"
fileprivate let FIELD_THRESHOLD = "threshold"
fileprivate let FIELD_FACE_WIDTH = "faceWidth"
fileprivate let FIELD_FACE_HEIGHT = "faceHeight"

/**
* Event emitted when the user face is too small to be eligible for
* face recognition task.
*/
public class OnFaceProximity : Event {
        
    private let emit: RCTDirectEventBlock?
    
    public required init(emit: RCTDirectEventBlock?) {
        self.emit = emit
    }
    
    public func send(data: ProximityResult) {
        let event: [String: Any] = [
            FIELD_IS_UNDER_THRESHOLD: data.isUnderThreshold,
            FIELD_THRESHOLD: data.threshold,
            FIELD_FACE_WIDTH: data.faceWidth,
            FIELD_FACE_HEIGHT: data.faceHeight
        ]
        emit?(event)
    }
}
