//
//  VisionObject.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/10/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import UIKit

/**
* [VisionObject] conceptually comes from an image as source. Source properties here
* carries information about the source image such as size and rotation needed to be
* applied to the image to have it upside right.
*
* These properties depends on the producer of the images and object.
*/
public protocol VisionObject {
    /**
     * Source image size where this [VisionObject] was found
     */
    var sourceSize: CGSize { set get }
    
    /**
     * Bounding box of this [VisionObject]. This value is based on the [sourceSize] coordinate.
     */
    var boundingBox: CGRect { set get }
}
