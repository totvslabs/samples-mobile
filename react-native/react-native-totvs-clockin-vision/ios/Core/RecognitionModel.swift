//
//  DetectionModel.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* Model as representative of model classes that are capable of detection
* tasks over an input value.
*/
public protocol RecognitionModel : Model  {
    associatedtype Input
    associatedtype Output
    
    /**
     * Perform detection over the input producing a list of detected entities of
     * type [Output]. Optionally also perform detection on the provided input
     *
     * This method might throw [IllegalStateException] if called before being the model
     * trained. This is implementation detail
     *
     * @discussion requires to run on a working thread.
     */
    func recognize(
        input: Input,
        includeDetection: Bool,
        onRecognized: @escaping (ModelOutput<Output>) -> Void
    ) throws
}
