//
//  FaceObject.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import UIKit

/**
* Vision object that represents a detected barcode.
*
* Encoded information on this object, depends on the kind of detector used.
*
* While designing this class we took the decision to leave [landmarks] as a data class
* param even though is not meant to be accessible to the exterior, because we want automatic
* consideration of it on copy, equals and hashCode generated code. This can lead to some
* interesting consequences, like the following:
* 1. The field is private to the caller but modifiable by the class itself
* 2. The caller have the power to freeze the list of landmarks. In this case, any attempt
* to modify the landmarks won't have any effect.
*/
public struct FaceObject : VisionObject, Equatable {
    public let width: CGFloat
    public let height: CGFloat
    public let eyesOpenProbability: EyesOpenProbability
    public var sourceSize: CGSize = .zero
    public var boundingBox: CGRect = .zero
    public let eulerZ: CGFloat = 0.0
    public let eulerY: CGFloat = 0.0
    public var landmarks = [Landmark]()
    
    public subscript(name: LandmarkName) -> Landmark? {
        
        /**
         * Retrieve any landmark on this face if it is present. If this face doesn't
         * have the requested [Landmark], then null is returned, the actual
         * landmark otherwise.
         *
         * If the receiver of this operation is [NullFaceObject] then
         * null will be returned for any [name]
         */
        get {
            guard self != NullFaceObject else {
                return nil
            }
            let landmark = landmarks.first(where: { l in l.name == name })
            // if somehow [NullLandmark] ended up here, we just return null
            return landmark?.isNull == false ? landmark : nil
        }
        
        /**
         * Set any landmark on this [FaceObject]. [landmark] suffix to register
         * the landmark into this face but is a language requirement that this
         * operator receives at least two operands.
         *
         * If the receiver of this operation is [NullFaceObject] then nothing
         * will happens and the operation won't modify the object. i.e won't register
         * the landmark.
         *
         * [NullLandmark] can't be registered into a face.
         */
        set {
            if self == NullFaceObject || newValue == NullLandmark || nil == newValue {
                return
            }
            // drop all the landmarks with same name
            landmarks.removeAll(where: { l in l.name == name })
            // add the fresh one
            landmarks.append(newValue!)
        }
    }
}

/**
 * Iterate over landmarks.
 */
extension FaceObject : Sequence {
    public typealias LandmarkIterator = Array<Landmark>.Iterator
    
    public func makeIterator() -> LandmarkIterator {
        return landmarks.makeIterator()
    }
}

extension FaceObject {
    var isNull : Bool { self == NullFaceObject }
}

/**
* Null representation of a null face object.
*/
public let NullFaceObject = FaceObject(
    width: 0.0, height: 0.0,
    eyesOpenProbability: EyesOpenProbability(left: 0.0, right: 0.0),
    sourceSize: CGSize(width: CGFloat.leastNormalMagnitude, height: .leastNormalMagnitude)
)

/**
* Eyes open probability
*/
public struct EyesOpenProbability : Equatable {
    public let left: CGFloat
    public let right: CGFloat
}
