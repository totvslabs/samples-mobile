//
//  ViewController.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import CameraCore
import CameraView

class ViewController: UIViewController {
    
    @IBOutlet var cameraView: CameraView!
    
    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }

    override func viewDidLoad() {
        super.viewDidLoad()
    
        cameraView = attachCamera()
    }
    
    private func attachCamera() -> CameraView {
        let cameraView = CameraView()
        view.insertSubview(cameraView, at: 0)
        
        cameraView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            cameraView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            cameraView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            cameraView.topAnchor.constraint(equalTo: view.topAnchor),
            cameraView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        return cameraView
    }

    @IBAction private func flipCamera(_ button: UIButton) {
        cameraView.toggleCamera()
    }
    
    @IBAction private func takePicture(_ button: UIButton) {
        cameraView.takePicture(with: OutputFileOptions()) { _, _ in
            print("Picture captured")
        }
    }
}

