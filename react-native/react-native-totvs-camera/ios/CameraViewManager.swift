
import Foundation
import UIKit
import TOTVSCameraKit

/// refs: /// refs: https://teabreak.e-spres-oh.com/swift-in-react-native-the-ultimate-guide-part-1-modules-9bb8d054db03
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

/// MARK: withView
extension CameraViewManager {
    func withView(_ node: NSNumber, action: @escaping (ReactCameraView) -> Void) {
        DispatchQueue.main.async {
            let view = self.bridge.uiManager.view(
                forReactTag: node
            ) as! ReactCameraView
            
            action(view)
        }
    }
}

/// MARK: Camera Contract
extension CameraViewManager {
    /**
     * Set camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @objc func setZoom(_ zoom: NSNumber,
        node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            view.zoom = Float(truncating: zoom)
            resolve(true)
        }
    }
    
    /**
     * Get camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @objc func getZoom(_ node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            resolve(NSNumber(value: view.zoom))
        }
    }
    
    /**
     * Enable or disable camera torch
     */
    @objc func enableTorch(_ enabled: Bool,
        node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            view.isTorchEnabled = enabled
            resolve(true)
        }
    }
    
    /**
     * whether or not the camera torch is enabled
     */
    @objc func isTorchEnabled(_ node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            resolve(view.isTorchEnabled)
        }
    }
    
    /**
     * Toggle the camera. i.e if the current camera is the front one it will toggle to back
     * camera as vice versa.
     */
    @objc func toggleCamera(
        _ node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            view.toggleCamera()
            resolve(true)
        }
    }
    
    /**
     * Change the camera lens display. This is related to [toggleCamera] in the sense
     * that this method indicate explicitly which lens to use for the camera.
     *
     * [facing] is expressed as one of the exported constants [CameraFacingConstants]
     */
    @objc func setLensFacing(_ facing: NSNumber,
        node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        guard let facing = CameraFacing(rawValue: Int(truncating: facing)) else {
            reject("E_FACING", "Invalid facing value", NSError(domain: "", code: 100, userInfo: nil))
            return
        }
        withView(node) { view in
            view.facing = facing
            resolve(true)
        }
    }
    
    /**
     * Get current camera facing. Returned facing is expected to be one of the exported
     * facing constants [CameraFacingConstants].
     */
    @objc func getLensFacing(_ node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            resolve(view.facing.rawValue)
        }
    }
    
    /**
     * Even though the [Camera] interface offers two variation of [takePicture]
     * here we only expose what's relevant to a react-native app at this moment. The
     * one that save the image into an specified location.
     *
     * This doesn't restrict from exposing the counter part capture method, we only need
     * to figure out how/what to send up to the app as a representation of the captured
     * image.
     */
    
    @objc func takePicture(_ outputDir: NSString,
        node: NSNumber,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        withView(node) { view in
            view.takePicture(with: OutputFileOptions()) { url, error in
                if nil != error {
                    reject("E_CAPTURE", "Error capturing still picture", error!)
                } else {
                    resolve("saved from native: <placeholder> text")
                }
            }
        }
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
