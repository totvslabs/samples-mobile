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
    
    private lazy var analzyzer = DetectionAnalyzer(
        queue: DispatchQueue(label: "DetectionThread", attributes: [.concurrent]),
        detectors: FaceDetector()
    )
    
    private var connection: Connection? = nil
    
    private lazy var faceBoundingBox = FaceBoundingBox(cameraView: cameraView)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initCameraView()
    }
    
    @IBAction func capture(_ button: UIButton) {
//        cameraView.takePicture(with: OutputFileOptions()) { (url, error) in
//            print("saved")
//        }
        installAnalyzer()
    }
    
    @IBAction func flipCamera(_ button: UIButton) {
//        cameraView.toggleCamera()
        uninstallAnalyzer()
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

/// MARK: Detection Analyzer
extension ViewController {
    func installAnalyzer() {
        connection = analzyzer
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .sendAsync(on: .main)
            .connect(faceBoundingBox)
            
        
        cameraView.analyzer = analzyzer
    }
    
    func uninstallAnalyzer() {
        connection?.dicsonnect()
        cameraView.analyzer = nil
    }
}
