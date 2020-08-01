//
//  FaceBoundingBox.swift
//  Example
//
//  Created by Jansel Rodriguez on 7/12/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import TOTVSCameraKit

class FaceBoundingBox : VisionReceiver<FaceObject> {
    
    private weak var cameraView: CameraView? = nil
    
    private lazy var boundingView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 10.0
        view.alpha = 0.3
        view.backgroundColor = .green
        return view
    }()
    
    init(cameraView: CameraView) {
        self.cameraView = cameraView
    }
    
    private func addBoundingView() {
        cameraView?.addOverlayGraphic(boundingView)
    }
    
    private func removeBoundingView() {
        boundingView.removeFromSuperview()
    }
    
    override func send(value: FaceObject) {
        guard value != NullFaceObject else {
            removeBoundingView()
            return
        }
        if boundingView.superview == nil {
            addBoundingView()
        }
        
        // this step is needed because we need a unit rectangle for the next step.
        let normalizedFrame = CGRect(
            x: value.boundingBox.origin.x / value.sourceSize.width,
            y: value.boundingBox.origin.y / value.sourceSize.height,
            width: value.boundingBox.width / value.sourceSize.width,
            height: value.boundingBox.height / value.sourceSize.height
        )
        let standardizedRect = cameraView?.previewRect(
            fromCaptureDeviceRect: normalizedFrame
        ).standardized
        
        UIView.animate(withDuration: 0.2) {
            self.boundingView.frame = standardizedRect!
        }
    }
}

