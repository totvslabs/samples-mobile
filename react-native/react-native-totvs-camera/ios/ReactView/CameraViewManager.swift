
import Foundation

@objc(CameraViewManager)
class CameraViewManager : RCTViewManager {
    
    override func view() -> UIView! {
        return ReactCameraView()
    }
    
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "LENS_FACING": LENS_FACING,
            "ZOOM_LIMITS": ZOOM_LIMITS
        ]
    }
    
    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
}

/// MARK: Exported Constants
fileprivate let LENS_FACING = [
    "FRONT": CameraFacing.front.rawValue,
    "BACK" : CameraFacing.back.rawValue
]

fileprivate let ZOOM_LIMITS = [
    "MIN": 0.0,
    "MAX": 1.0
]
