//
//  ViewController.swift
//  Example
//
//  Created by Jansel Rodriguez on 7/6/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import TOTVSCameraKit
import MLKit

class ViewController: UIViewController {
    
    private var cameraView: CameraView!
    
    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }
    
    fileprivate var detectorOptions: FaceDetectorOptions {
        let options = FaceDetectorOptions()
        options.performanceMode = .fast
        options.contourMode = .none
        options.landmarkMode = .all
        options.classificationMode = .all
        
        return options
    }
    
    fileprivate lazy var faceDetector = FaceDetector.faceDetector(options: detectorOptions)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initCameraView()
    }
    
    @IBAction func capture(_ button: UIButton) {
//        cameraView.takePicture(with: OutputFileOptions()) { (url, error) in
//            print("saved")
//        }
        cameraView.analyzer = self
    }
    
    @IBAction func flipCamera(_ button: UIButton) {
//        cameraView.toggleCamera()
        cameraView.analyzer = nil
    }
}

/// MARK: Init CameraView
extension ViewController {
    private func initCameraView() {
        cameraView = CameraView()
//        cameraView.facing = .front
        
        view.insertSubview(cameraView, at: 0)
        
        cameraView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            cameraView.leftAnchor.constraint(equalTo: view.leftAnchor),
            cameraView.rightAnchor.constraint(equalTo: view.rightAnchor),
            cameraView.topAnchor.constraint(equalTo: view.topAnchor),
            cameraView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }
}

/// MARK: Image Analyzer
extension ViewController : ImageAnalyzer {
    func analyze(image: ImageProxy) {
        process(image: image)
    }
}

/// MARK: Firebase Vision
extension ViewController {
    func process(image: ImageProxy) {
        
        guard let buffer = image.buffer else {
             return
        }
                
        image.use { _ in
            let visionImage = VisionImage(buffer: buffer)
            visionImage.orientation = visionImageOrientation(facing: cameraView.facing, imageOrientation: image.imageInfo.orientation)
            
            var faces: [Face]
            do {
                faces = try faceDetector.results(in: visionImage)
            } catch let error {
                print("error fetching faces \(error)")
                return
            }
            DispatchQueue.main.async {
                self.removeOverlays()
            }
            guard !faces.isEmpty else {
              print("On-Device face detector returned no results.")
              return
            }
            DispatchQueue.main.async {
                self.renderOverlays(of: faces, width: CGFloat(image.width), height: CGFloat(image.height))
            }
        }
    }
    
    func removeOverlays() {
        for v in cameraView.graphicOverlay.subviews {
            v.removeFromSuperview()
        }
    }
    
    func renderOverlays(of faces: [Face], width: CGFloat, height: CGFloat) {
        for face in faces {             
            // this step is needed because we need a unit rectangle for the next step.
            let normalizedFrame = CGRect(
                x: face.frame.origin.x / width,
                y: face.frame.origin.y / height,
                width: face.frame.size.width / width,
                height: face.frame.size.height / height
            )
            let standardizedRect = cameraView.previewRect(
                fromCaptureDeviceRect: normalizedFrame
            ).standardized
            
            let view = UIView(frame: standardizedRect)
            view.layer.cornerRadius = 10.0
            view.alpha = 0.3
            view.backgroundColor = .green
            cameraView.addOverlayGraphic(view)
            
//            var faceLayer = layer.sublayers?.first(where: { $0.name == "Face" }) as? CAShapeLayer
//
//            let path = UIBezierPath(rect: face.frame)
//
//            if nil == faceLayer {
//                faceLayer = CAShapeLayer()
//                faceLayer?.lineWidth = 1
//                faceLayer?.strokeColor = UIColor.red.cgColor
//                faceLayer?.path = path.cgPath
//
//                layer.addSublayer(faceLayer!)
//            } else {
//                faceLayer?.path = path.cgPath
//            }
        }
    }
    
    /// Get the right orientation contained in a CMSampleBuffer image
    func visionImageOrientation(facing: CameraFacing, imageOrientation: UIDeviceOrientation) -> UIImage.Orientation {
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
