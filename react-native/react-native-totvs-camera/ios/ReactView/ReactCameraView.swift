

import UIKit

@objc(ReactCameraView)
class ReactCameraView : CameraView { }

/// MARK: View property setters.
extension ReactCameraView {
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
