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
    
    //Bound is used to determine if the users face has returned to the
    // center of the screen.
    static let EULER_ANGLE_CENTER_BOUND: CGFloat = 5
    
    let infeasibleAreaPercent: Float
    
    open var onLiveness: ((LivenessResult) -> Void)? = nil
    
    private let state = YawState()
    
    public init(infeasibleAreaPercent: Float = 0.2) {
        self.infeasibleAreaPercent = infeasibleAreaPercent
    }
    
    public init(infeasibleAreaPercent: Float = 0.2, onLiveness: @escaping (LivenessResult) -> Void) {
        self.infeasibleAreaPercent = infeasibleAreaPercent
        self.onLiveness = onLiveness
    }
    
    open override func send(value: FaceObject) {
        // we only consider faces with noses landmark here
        guard let nose = value[.nose], NullFaceObject != value else {
            return
        }
        // already translated by [FaceNoseTranslator]
        let cx = nose.position.x
        // y-euler face rotation
        let eulerY = value.eulerY
        
        let boundary = value.sourceSize.width * CGFloat(infeasibleAreaPercent)
        // If the nose landmark x coordinate is within bounds proceed.
        if boundary < cx && cx < (value.sourceSize.width - boundary) {
            state.push(yaw: Int(eulerY)) { // if activated
                if -LivenessFace.EULER_ANGLE_CENTER_BOUND < eulerY && eulerY < LivenessFace.EULER_ANGLE_CENTER_BOUND {
                    // is live. clear
                    state.clear()
                    // emit event
                    onLiveness?(LivenessResult(mode: LivenessFace.id))
                }
            }
        } else {
            state.clear()
        }
    }
}

/**
 State for the face yaw movement
 */
fileprivate class YawState {
    // controls the yaw activation function
    static let YAW_THRESHOLD = 13
    
    private var yaws = [Int]()
    
    func push(yaw: Int, onActivated: () -> Void) {
        yaws.append(yaw)
        if (isActivated()) {
            onActivated()
        }
    }
    
    func clear() {
        yaws.removeAll()
    }
    
    private func isActivated() -> Bool {
        let yaws = self.yaws.sorted()
        
        // Since the array is ordered from negative to positive.
        guard !yaws.isEmpty else {
            return false
        }
        return -YawState.YAW_THRESHOLD >= yaws.first! && yaws.last! >= YawState.YAW_THRESHOLD
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
    
    /**
     * non-computed probability
     */
    static let UNCOMPUTED_PROBABILITY: CGFloat = -1.0
    
    /**
     * Value to define if eyes are closed.
     */
    static let EYES_CLOSED_THRESHOLD: CGFloat = 0.6

    /**
     * Value to define if eyes are open.
     */
    static let EYES_OPEN_THRESHOLD: CGFloat = 0.90
    
    
    open var requiredBlinks: Int = LivenessEyes.NO_BLINKS
    
    open var onLiveness: ((LivenessResult) -> Void)? = nil
    
    /**
     * Keep track of the previous probability tracked.
     */
    private var previousEyesOpenProb: CGFloat = 0.0

    /**
     * Keep track of the blink count before emitting an event.
     */
    private var blinks = 0
    
    public override init() {
    }
    
    public init(requiredBlinks: Int, onLiveness: @escaping (LivenessResult) -> Void) {
        self.requiredBlinks = requiredBlinks
        self.onLiveness = onLiveness
    }
    
    open override func send(value: FaceObject) {
        // we try not to perform unnecessary work
        guard requiredBlinks != LivenessEyes.NO_BLINKS && NullFaceObject != value else {
            return
        }
        if meetRequiredBlinks(face: value) {
            onLiveness?(LivenessResult(mode: LivenessEyes.id))
        }
    }
    
    private func meetRequiredBlinks(face: FaceObject) -> Bool {
        // Get the lowest value from both eyes.
        let eyesOpenProb = min(face.eyesOpenProbability.left, face.eyesOpenProbability.right)
        
        guard eyesOpenProb != LivenessEyes.UNCOMPUTED_PROBABILITY else {
            return false
        }
        // Were the eyes open previously?
        var meetRequirement = false
        
        if previousEyesOpenProb > LivenessEyes.EYES_OPEN_THRESHOLD {
            // Are the eyes closed?
            if eyesOpenProb < LivenessEyes.EYES_CLOSED_THRESHOLD {
                blinks += 1
                
                if blinks >= requiredBlinks {
                    meetRequirement = true
                    blinks = 0
                }
            }
        }
        previousEyesOpenProb = eyesOpenProb
        return meetRequirement
    }
}


extension LivenessEyes : CustomStringConvertible {
    public var description: String { "LivenessEyes" }
}
