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
*/
public protocol Face {
    /**
     * Label of the face object. meta-info
     */
    var label: String? { get }

    /**
     * Confidence of this face object. this might mean confidence of detection
     * or confidence of recognition.
     */
    let confidence: Float { get }

    /**
     * Bounding box determining this face on the provided source where this face object
     * was detected/recognized
     */
    var boundingBox: CGRect { get }

    /**
     * Encoding of the face. meta-info
     */
    var encoding: String? { get }
}
