//
//  PreviewView.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import AVFoundation

/**
 * View to use as camera device output previewer
 */
class PreviewView : UIView {
    
    /**
     @property videoPreviewLayer
     @abstract
        This view layer as a `AVCaptureVideoPreviewLayer`
     */
    var videoPreviewLayer: AVCaptureVideoPreviewLayer {
        guard let layer = layer as? AVCaptureVideoPreviewLayer else {
            fatalError("Expected: `AVCaptureVideoPreviewLayer` type for layer. Check PreviewView.layerClass implementation.")
        }
        return layer
    }
    
    /**
     @property session
     @abstract
        Current capture device session
     */
    var session: AVCaptureSession? {
        get { videoPreviewLayer.session }
        set { videoPreviewLayer.session = newValue }
    }
    
    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }
}
