

import UIKit
import TOTVSCameraKit

@objc(ReactCameraView)
class ReactCameraView : CameraView { }

/// MARK: View property setters.
/// refs: https://teabreak.e-spres-oh.com/swift-in-react-native-the-ultimate-guide-part-1-modules-9bb8d054db03
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
