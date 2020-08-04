//
//  Landmark.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

public enum LandmarkName : Int {
    case leftEye
    case rightEye
    case nose
    case unknown
}

/**
* Representation of face landmarks
*/
public struct Landmark : Equatable {
    /**
     * The name of this landmark
     */
    public var name: LandmarkName
    
    /**
     * Position of this landmark
     */
    public var position: CGPoint
}

/**
* Null representation of a landmark. This value is used when purely null values are not
* supported. This value was specifically created for [FaceObject.get] but we there relaxed
* the constraint in order to support generic typed return.
*/

public let NullLandmark = Landmark(
    name: .unknown,
    position: CGPoint(x: CGFloat.leastNormalMagnitude, y: .leastNormalMagnitude)
)

/**
* Accessor to know when this [Landmark] is null
*/
extension Landmark {
    var isNull: Bool { self == NullLandmark }
}

