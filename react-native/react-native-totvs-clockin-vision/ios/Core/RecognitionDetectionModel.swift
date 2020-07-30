//
//  FaceRecognitionDetectionModel.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
 Abstract class for a generic Recognition & Detection model
 */
public class RecognitionDetectionModel<Input, Output> : RecognitionModel, DetectionModel {
    public var isTrained: Bool = false
    
    public func initialize() {
        fatalError("Abstract Method")
    }
    
    public func train() {
        fatalError("Abstract Method")
    }
    
    public func release() {
        fatalError("Abstract Method")
    }
    
    public func detect(input: Input, onDetected: @escaping ([Output]) -> Void) throws {
        fatalError("Abstract Method")
    }
    
    public func recognize(input: Input, onRecognized: @escaping ([Output]) -> Void) throws {
        fatalError("Abstract Method")
    }
}
