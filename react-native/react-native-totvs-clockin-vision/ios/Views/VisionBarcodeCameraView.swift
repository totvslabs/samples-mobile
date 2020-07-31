//
//  VisionBarcodeCameraView.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/31/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import TOTVSCameraKit

/**
* @TODO (jansel) implement barcode camera capabilities
*/

@objc(VisionBarcodeCameraView)
class VisionBarcodeCameraView : CameraView, VisionBarcodeCamera { }

/// MARK: View JS Properties
extension VisionBarcodeCameraView {
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
