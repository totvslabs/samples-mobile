//
//  VisionFaceCameraViewManager.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import TOTVSCameraKit

@objc(VisionFaceCameraViewManager)
class VisionFaceCameraViewManager : RCTViewManager {
        
    override func view() -> UIView! {
        return VisionFaceCameraView()
    }
    
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "LENS_FACING": LENS_FACING,
            "ZOOM_LIMITS": ZOOM_LIMITS,
            "LIVENESS_MODE": LIVENESS_MODE
        ]
    }
    
    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
}

/// MARK: withView
extension VisionFaceCameraViewManager {
    func withView(_ node: NSNumber, action: @escaping (VisionFaceCameraView) -> Void) {
        DispatchQueue.main.async {
            let view = self.bridge.uiManager.view(
                forReactTag: node
            ) as! VisionFaceCameraView
            
            action(view)
        }
    }
}

/// MARK: Camera Contract
extension VisionFaceCameraViewManager {
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

fileprivate let LIVENESS_MODE = [
    "NONE": 0
]
