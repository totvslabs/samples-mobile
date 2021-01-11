//
//  DetectionModel.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* Model as representative of model classes that are capable of recognition
* tasks over an input value.
*/
public protocol DetectionModel : Model  {
    associatedtype Input
    associatedtype Output
    
    /**
     * Perform recognition over the input producing a list of recognized entities of
     * type [Output].
     *
     * This method might throw [IllegalStateException] if called before being the model
     * trained. This is implementation detail
     *
     * @discussion requires to run on a working thread.
     */
    func detect(input: Input, onDetected: @escaping (ModelOutput<Output>) -> Void) throws
}
