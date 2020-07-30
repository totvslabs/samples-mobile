//
//  ModelProvider.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* Model provider for all the models that this library uses.
*/
public class ModelProvider {
    private init() {
    }
    
    /**
     * Returns a face recognition model
     */
    static func getFaceRecognitionDetectionModel(config: ModelConfig) -> RecognitionDetectionModel<String, Face> {
        return NativeFaceModel.default
    }
}
