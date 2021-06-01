//
//  FaceTransformer.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/4/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import TOTVSCameraKit

/**
* Scaler to translate the nose landmark. This scaler is needed for receivers that need face objects
* landmarks in the same coordinate system as [GraphicOverlay].
*
* This translator is only used for liveness face since we need to do some processing on
* the nose coordinate.
*/
public class FaceNoseTranslator : Transformer {
    private weak var cameraView: CameraView? = nil
    
    public init(cameraView: CameraView) {
        self.cameraView = cameraView
    }
    
    public func transform(value: FaceObject, receiver: VisionReceiver<FaceObject>) {
        // we do nothing when we receives a null face
        guard NullFaceObject != value, nil != cameraView else {
            return receiver.send(value: value)
        }
        var face = value
        // we do nothing when we don't have a nose
        guard let nose = face[.nose] else {
            return receiver.send(value: face)
        }
        let normalized = normalize(point: nose.position, sourceSize: face.sourceSize)
        
        face[.nose] = Landmark(name: .nose, position: normalized)
        face.sourceSize = cameraView!.frame.size
        
        receiver.send(value: face)
    }
}


fileprivate extension FaceNoseTranslator {
    // normalize points comming from camera device coordinate system.
    private func normalize(point: CGPoint, sourceSize: CGSize) -> CGPoint {
        // this step is required to create a unit point
        let normalized = CGPoint(x: point.x / sourceSize.width, y: point.y / sourceSize.height)
        return cameraView!.previewPoint(fromCaptureDevicePoint: normalized)
    }
}
