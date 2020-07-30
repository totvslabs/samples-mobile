//
//  Model.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* Model as representative of model classes that are capable of being trained and released.
*
* @see [RecognitionModel] and [DetectionModel]
*/
public protocol Model {
    /**
     * Indicate whether or not this model is already trained.
     */
    var isTrained: Bool { get }
    
    /**
     * Initialize the model accordingly. After the call to this method
     * the model might be ready to perform other operations on its inputs.
     * If this is the case then [isTrained] might return true after a call to this method.
     *
     * Is advisable to call [train] and not rely on this method to have the model
     * trained. Is up to the implementation if [train] will have any effect after
     * calling this method.
     *
     * @discussion requires to run on a working thread.
     */
    func initialize()
    
    /**
     * Train the model and leave it ready for further operations.
     *
     * @see [RecognitionModel] and [DetectionModel]
     *
     * @discussion requires to run on a working thread.
     */
    func train()
    
    /**
     * Release the trained model. It's important to release the model after no longer needed
     * because this might incur in memory release.
     *
     * @discussion requires to run on a working thread.
     */
    func release()
}

