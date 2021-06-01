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
open class RecognitionDetectionModel<Input, Output> : RecognitionModel, DetectionModel {
    public init() { }
    
    open var isTrained: Bool = false
    
    open func configure(with config: ModelConfig) {
    }
    
    open func initialize() {
    }
    
    open func train() {
    }
    
    open func releaseResources() {
    }
    
    open func detect(
        input: Input,
        onDetected: @escaping (ModelOutput<Output>) -> Void
    ) throws { }
    
    open func recognize(
        input: Input,
        includeDetection: Bool = true,
        onRecognized: @escaping (ModelOutput<Output>) -> Void
    ) throws { }
}