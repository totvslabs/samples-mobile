//
//  ImageInfo.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/8/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import UIKit

/** Metadata for an image. */
public protocol ImageInfo {
    
    /**
     @property timestamp
     @abstract
        Timestamp of the taken image.
    */
    var timestamp: Int64 { get }
    
    /**
     @property orientation
     @abstract
        Returns the orientation needed to transform the image to the correct orientation.
     
     @discussion
        The target orientation is set at the time the image capture was requested.
     */
    var orientation: UIDeviceOrientation { get }
}
