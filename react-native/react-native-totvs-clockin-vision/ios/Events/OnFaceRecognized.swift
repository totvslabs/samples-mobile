//
//  OnFaceRecognized.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/3/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* Event emitted when face recognition is triggered.
*/
public class OnFaceRecognized : Event {
    
    private let emit: RCTDirectEventBlock?
    
    public required init(emit: RCTDirectEventBlock?) {
        self.emit = emit
    }
    
    public func send(data: RecognitionResult) {
    }
}
