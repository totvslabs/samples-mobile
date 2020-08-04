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
    let isUnderThreshold: Bool
    
    public init(threshold: Float, faceWidth: Float, faceHeight: Float, isUnderThreshold: Bool = false) {
        self.threshold = threshold
        self.faceWidth = faceWidth
        self.faceHeight = faceHeight
        self.isUnderThreshold = isUnderThreshold
    }
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
        didSet { }
    }
    
    open var onProximity: ((ProximityResult) -> Void)? = nil
    
    /**
     * Control whether we've found a face or not.
     *
     * We use this for situations when the last event we emitted was a
     * valid event for either a right or wrong face and then no face is found.
     * We want to clean that previous state by emitting one event indicating that we're
     * not matching (finding) faces anymore. This allows us to send only one event of this type
     * since it might be the one with the most occurrence.
     *
     * We start with the assumption that we have found a face and let's the
     * detection disprove that assumption
     */
    private var foundFace = true
    
    public override init() {
    }
    
    public init(threshold: Float, onProximity: @escaping (ProximityResult) -> Void) {
        self.threshold = threshold
        self.onProximity = onProximity
    }
    
    open override func send(value: FaceObject) {
        if value == NullFaceObject {
            if foundFace {
                foundFace = false
                onProximity?(ProximityResult(
                    threshold: threshold,
                    faceWidth: 0.0,
                    faceHeight: 0.0
                ))
            }
        } else {
            foundFace = true
            // send a valid event.
            onProximity?(ProximityResult(
                threshold: threshold,
                faceWidth: Float(value.width),
                faceHeight: Float(value.height),
                isUnderThreshold: Float(value.width) <= threshold
            ))
        }
    }
}


extension ProximityByFaceWidth : CustomStringConvertible {
    public var description: String { "ProximityByFaceWidth" }
}
