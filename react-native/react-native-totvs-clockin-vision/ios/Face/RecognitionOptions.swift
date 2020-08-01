//
//  RecognitionOptions.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/1/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
 * Options to control aspects of the recognition on still pictures.
 */
public struct RecognitionOptions {
    /**
     Whether we need to save the captured image or not.
     */
    let saveImage: Boolean = false
    /**
     Output directory location where to save the image.
     */
    let outputDir: URL? = nil
}
