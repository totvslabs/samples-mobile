//
//  FaceDetector.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import MLKitVision
import MLKitFaceDetection

fileprivate typealias MLKitFaceDetector = MLKitFaceDetection.FaceDetector

/**
* Detector dedicated to identity faces. This detector is a _Single emission_ detector.
*
* This detector is focused in classifications and landmark detections, following the
* recommendations on:
* https://developers.google.com/ml-kit/vision/face-detection/android#performance_tips
*
* If a contour or another kind of detector is required, this detector offers an interface
* for subclasses to customize the detector capabilities.
*
* As-is this detector keeps a singleton of a high-accuracy detector.
*
* This detector relies on the new Firebase detection API.
*
* @see [VisionDetector]
*/
open class FaceDetector : VisionDetector {
    
    public static let `default` = FaceDetector()
    
    public static var key: AnyHashable { DETECTOR_KEY }
    
    private let selectFace: SelectionStrategy<Face> = SelectMostProminent
    
    private var detectorOptions: FaceDetectorOptions {
        let options = FaceDetectorOptions()
        options.performanceMode = .accurate
        options.contourMode = .none
        options.landmarkMode = .all
        options.classificationMode = .all
        
        return options
    }
    
    private lazy var detector = MLKitFaceDetector.faceDetector(options: detectorOptions)
    
    private init() { }
    
    public func detect(on queue: DispatchQueue, image: ImageProxy, onDetected: (VisionObject) -> Void) {
        guard let buffer = image.buffer else {
            onDetected(NullFaceObject)
            return
        }
        
        let visionImage = VisionImage(buffer: buffer)
        visionImage.orientation = imageOrientation(
            facing: image.imageInfo.sourceFacing,
            imageOrientation: image.imageInfo.orientation
        )
        
        var faces = [Face]()
        do {
            faces = try detector.results(in: visionImage)
        } catch let error {
            print("FaceDetector for error at detecting faces: \(error)")
        }
                
        guard !faces.isEmpty, let face = selectFace(faces) else {
            onDetected(NullFaceObject)
            return
        }
        onDetected(mapToFaceObject(
            face: face,
            imageWidth: image.width,
            imageHeight: image.height
        ))
    }
    
    /**
     * Map the MLKIT vision face to face object
     */
    private func mapToFaceObject(face: Face, imageWidth: Int, imageHeight: Int) -> FaceObject {
        return FaceObject(
            width: face.frame.width,
            height: face.frame.height,
            eyesOpenProbability: EyesOpenProbability(
                left: face.leftEyeOpenProbability,
                right: face.rightEyeOpenProbability
            ),
            sourceSize: CGSize(width: imageWidth, height: imageHeight),
            boundingBox: face.frame,
            landmarks: extractLandmarks(from: face)
        )
    }
    
    /**
     * Extract all the recognized landmarks. We consider a landmark as recognized
     * if there's a corespondent type [Landmark] for it. If there isn't then the landmark is
     * ignored
     */
    private func extractLandmarks(from face: Face) -> [Landmark] {
        var landmarks = [Landmark]()
        
        if let l = face.landmark(ofType: .leftEye) {
            landmarks.append(Landmark(name: .leftEye, position: l.position.toPoint))
        }
        if let r = face.landmark(ofType: .rightEye) {
            landmarks.append(Landmark(name: .rightEye, position: r.position.toPoint))
        }
        if let n = face.landmark(ofType: .noseBase) {
            landmarks.append(Landmark(name: .nose, position: n.position.toPoint))
        }
        return landmarks
    }
    
    /// Get the right orientation contained in a CMSampleBuffer image
    private func imageOrientation(facing: CameraFacing, imageOrientation: UIDeviceOrientation) -> UIImage.Orientation {
        switch imageOrientation {
        case .portrait:
            return facing == .front ? .leftMirrored : .right
        case .landscapeLeft:
            return facing == .front ? .downMirrored : .up
        case .portraitUpsideDown:
            return facing == .front ? .rightMirrored : .left
        case .landscapeRight:
            return facing == .front ? .upMirrored : .down
        case .faceDown, .faceUp, .unknown:
            return .up
        @unknown default: fatalError()
        }
    }
}

fileprivate extension VisionPoint {
    var toPoint: CGPoint { CGPoint(x: x, y: y) }
}

fileprivate let DETECTOR_KEY = 130290

/**
 * Strategy for selecting the most prominent face
 */
fileprivate let SelectMostProminent : SelectionStrategy<Face> = { (objects: Array<Face>) in objects.first }
