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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initCameraView()
    }
    
    @IBAction func capture(_ button: UIButton) {
        cameraView.takePicture(with: OutputFileOptions()) { (url, error) in
            print("saved")
        }
    }
    
    @IBAction func flipCamera(_ button: UIButton) {
        cameraView.toggleCamera()    
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

