//
//  ImageAnalyzer.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

/**
 * Interface to plug analysis on the camera device.
 */
public protocol ImageAnalyzer {
    /**
     @method analyze(image:):
     @abstract
        Analyze images received from the camera device.
     
     @discussion
        `ImageProxy` received here needs to be properly closed by the receiver in order to guarantee
         implementation behavior.
     */
    func analyze(image: ImageProxy)
}
