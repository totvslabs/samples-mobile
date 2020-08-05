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
open class RecognitionDetectionModel<Input, Output> : NSObject, RecognitionModel, DetectionModel {
    public var isTrained: Bool = false
    
    open func configure(with: ModelConfig) {
        fatalError("Abstract Method")
    }
    
    open func initialize() {
        fatalError("Abstract Method")
    }
    
    open func train() {
        fatalError("Abstract Method")
    }
    
    open func releaseResources() {
        fatalError("Abstract Method")
    }
    
    open func detect(input: Input, onDetected: @escaping ([Output]) -> Void) throws {
        fatalError("Abstract Method")
    }
    
    open func recognize(input: Input, onRecognized: @escaping ([Output]) -> Void) throws {
        fatalError("Abstract Method")
    }
}
