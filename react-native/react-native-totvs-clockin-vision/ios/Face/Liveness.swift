//
//  Liveness.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/31/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import TOTVSCameraKit

/**
* Liveness detection mode.
*/
open class Liveness : VisionReceiver<FaceObject> { }

/**
* Data representing the result of a liveness detection
*/
public struct LivenessResult {
    let mode: Int
}

/**
* Detect face movement
*
* @param infeasibleAreaPercent controls what horizontal percentage of the screen
* will be regarded as not feasible region for the face to be to be even considered
* for liveness. This parameter help us to deal with the situation that one face might
* be in a corner of the screen coordinate system and we can't recognize it quite well.
*/
open class LivenessFace : Liveness {
    internal static let id = 1
    
    let infeasibleAreaPercent: Float
    open var onLiveness: ((LivenessResult) -> Void)? = nil
    
    public init(infeasibleAreaPercent: Float = 0.2) {
        self.infeasibleAreaPercent = infeasibleAreaPercent
    }
    
    public init(infeasibleAreaPercent: Float = 0.2, onLiveness: @escaping (LivenessResult) -> Void) {
        self.infeasibleAreaPercent = infeasibleAreaPercent
        self.onLiveness = onLiveness
    }
}

extension LivenessFace : CustomStringConvertible {
    public var description: String { "LivenessFace" }
}

/**
* Detect eyes blinking
*/
open class LivenessEyes : Liveness {
    internal static let id = 2
    
    /**
     * Value that defined no required blinks
     */
    static let NO_BLINKS = -1
    
    open var requiredBlinks: Int = LivenessEyes.NO_BLINKS
    
    open var onLiveness: ((LivenessResult) -> Void)? = nil
    
    public override init() {
    }
    
    public init(requiredBlinks: Int, onLiveness: @escaping (LivenessResult) -> Void) {
        self.requiredBlinks = requiredBlinks
        self.onLiveness = onLiveness
    }
}


extension LivenessEyes : CustomStringConvertible {
    public var description: String { "LivenessEyes" }
}
