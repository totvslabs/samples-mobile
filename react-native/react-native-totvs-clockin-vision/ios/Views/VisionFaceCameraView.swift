//
//  VisionFaceCameraView.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import TOTVSCameraKit

@objc(VisionFaceCameraView)
class VisionFaceCameraView : CameraView, VisionFaceCamera { }

/// MARK: View JS Properties
extension VisionFaceCameraView {
    /**
     This properties are exposed to JS as part of the view component properties.
     They serve the same purpose of setter in the ViewManager on android.
     */
    
    @objc func setFacing(_ value: NSNumber) {
        if let facing = CameraFacing(rawValue: Int(truncating: value)) {
            self.facing = facing
        }
    }
    
    @objc func setZoom(_ value: NSNumber) {
        zoom = Float(truncating: value)
    }
}
