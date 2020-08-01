//
//  Proximity.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/1/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import TOTVSCameraKit

/**
* Proximity Detector
*/
open class Proximity : VisionReceiver<FaceObject> { }

/**
 * Data representing a proximity detection.
 */
public struct ProximityResult {
    let threshold: Float
    let faceWidth: Float
    let faceHeight: Float
    let isUnderThreshold: Bool = false
}

/**
* Proximity Detector that act on [FaceObject] width dimensions.
*/
open class ProximityByFaceWidth : Proximity {
    
    /**
     * Value that limit the face width to determine when the face width is right, hence
     * regarded as close.
     */
    var threshold: Float = 100.0 {
        didSet {
        }
    }
    
    let onProximity: (ProximityResult) -> Void
    
    init(onProximity: @escaping (ProximityResult) -> Void) {
        self.onProximity = onProximity
    }
}
