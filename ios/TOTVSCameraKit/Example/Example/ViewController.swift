//
//  ViewController.swift
//  Example
//
//  Created by Jansel Rodriguez on 7/6/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import TOTVSCameraKit

class ViewController: UIViewController {
    
    private var cameraView: CameraView!
    
    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }
    
    fileprivate lazy var faceDetector = CIDetector(
        ofType: CIDetectorTypeFace,
        context: nil,
        options: [
            CIDetectorAccuracy: CIDetectorAccuracyHigh,
            CIDetectorTracking: false,
        ]
    )!
    
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
        guard let buffer = image.buffer else {
            return
        }
        image.use { _ in
            let ciImage = CIImage(cvPixelBuffer: buffer)
            let features = faceDetector.features(in: ciImage, options: [
                CIDetectorSmile: true,
                CIDetectorEyeBlink: true
            ]).compactMap({ $0 as? CIFaceFeature })
        }
    }
}
