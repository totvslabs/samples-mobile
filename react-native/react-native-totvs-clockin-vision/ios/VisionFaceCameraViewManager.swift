//
//  VisionFaceCameraViewManager.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

@objc(VisionFaceCameraViewManager)
class VisionFaceCameraViewManager : RCTViewManager {
        
    override func view() -> UIView! {
        return VisionFaceCameraView()
    }
    
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
        ]
    }
    
    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
}
