//
//  CameraView.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import CameraCore

open class CameraView : UIView {

    /// CameraDevice preview view
    private(set) var previewView: PreviewView!
    
    /// CameraSource module that handle the camera device
    private lazy var cameraSource = CameraSource(cameraView: self)
        
    /// Current device orienation
    internal var windowOrientation: UIInterfaceOrientation {
        return window?.windowScene?.interfaceOrientation ?? .unknown
    }
    
    /// Keep track of focus points
    private var focusRecogninzer: UITapGestureRecognizer!
    
    /// MARK: init
    public override init(frame: CGRect) {
        super.init(frame: frame)
        applyInit()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        applyInit()
    }
    
    open override func removeFromSuperview() {
        cameraSource.stopRunning()
    }
    
    open override func didMoveToSuperview() {
        cameraSource.startRunning()
    }
}

/// MARK: Initialization
private extension CameraView {
    func applyInit() {
        previewView = addPreviewView()
        
        focusRecogninzer = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        previewView.addGestureRecognizer(focusRecogninzer)
    }
    
    func addPreviewView() -> PreviewView {
        let previewView = PreviewView()
        /// drop inferred constraints.
        previewView.translatesAutoresizingMaskIntoConstraints = false
        /// set the camera device to fill the view.
        previewView.videoPreviewLayer.videoGravity = .resizeAspectFill
        
        addSubview(previewView)
        /// properly adjust preview view.
        NSLayoutConstraint.activate([
            previewView.leadingAnchor.constraint(equalTo: leadingAnchor),
            previewView.trailingAnchor.constraint(equalTo: trailingAnchor),
            previewView.topAnchor.constraint(equalTo: topAnchor),
            previewView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
        return previewView
    }
}


/// MARK: Recognizers
extension CameraView {
    @objc private func handleTap(_ recognizer: UITapGestureRecognizer) {
        cameraSource.focusAndExpose(at: recognizer.location(in: self))
    }
}

/// MARK: Camera Contract
extension CameraView : Camera {
    public var isTorchEnabled: Bool {
        get { cameraSource.isTorchEnabled }
        set { cameraSource.isTorchEnabled = newValue }
    }
    
    public var zoom: Float {
        get { cameraSource.zoom }
        set { cameraSource.zoom = newValue }
    }
    
    public var facing: CameraFacing {
        set { cameraSource.facing = newValue }
        get { cameraSource.facing }
    }
    
    public func toggleCamera() {
        cameraSource.toggleCamera()
    }
    
    public func takePicture(_ onCaptured: @escaping OnImageCaptured) {
        cameraSource.takePicture(onCaptured)
    }
    
    public func takePicture(with options: OutputFileOptions, onSaved: @escaping OnImageSaved) {
        cameraSource.takePicture(with: options, onSaved: onSaved)
    }
}
