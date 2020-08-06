//
//  Face.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit

/**
* Clock-In specific purpose face object. This model class conforms specific
* IPC format used for faces.
*
* Do notice that unlike android counterpart interface this one mirror the new c++ recognition
* return type protocol
*/
public protocol Face {
    /**
     * Name of the person recognized
     */
    var name: String { get }
    
    /**
     * Name of the person recognized
     */
    var personId: String { get }

    /**
     * Confidence of this face object. this might mean confidence of detection
     * or confidence of recognition.
     */
    var distance: Float { get }

    /**
     * Encoding of the face. meta-info
     */
    var encoding: String { get }
}
