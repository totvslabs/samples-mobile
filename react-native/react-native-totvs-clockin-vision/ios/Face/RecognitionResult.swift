//
//  RecognitionResult.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/1/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
 * Result of a recognition tasks.
 */
public struct RecognitionResult {
    /**
     path in which the image was saved.
     */
    var imagePath: URL? = nil
    /**
     Faces recognized from the image.
     */
    var faces = [Face]()
}
