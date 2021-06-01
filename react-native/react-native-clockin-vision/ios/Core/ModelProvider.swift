//
//  ModelProvider.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import UIKit

/**
* Model provider for all the models that this library uses.
*/
public class ModelProvider {
    private static var model: RecognitionDetectionModel<UIImage, Face>? = nil
    
    private init() {
    }
    
    /**
     * Interface to inject a recognition model into the vision library.
     * The is assumed to provide an implementation for of the model to use by the
     * vision library.
     */
    public static func setFaceRecognitionDetectionModel(model: RecognitionDetectionModel<UIImage, Face>) {
        ModelProvider.model = model
    }
    
    /**
     * Returns a face recognition model
     */
    public static func getFaceRecognitionDetectionModel(config: ModelConfig) -> RecognitionDetectionModel<UIImage, Face> {
        precondition(nil != model, """
                RecognitionDetectionModel haven't being provided,
                you need to call getFaceRecognitionDetectionModel with a valid implementation
        """)
        model!.configure(with: config)
        return model!
    }
}