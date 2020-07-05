//
//  CaptureCallbacks.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
 * Callback to indicate when the image has been saved
 */
public typealias OnImageSaved = (_ file: URL?, _ error: Error?) -> Void

/**
 * Callback to indicate when an image was captured.
 */
public typealias OnImageCaptured = (_ image: ImageProxy?, _ error: Error?) -> Void
