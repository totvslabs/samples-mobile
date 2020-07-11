//
//  CameraView.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

open class CameraView : UIView {

    /// CameraDevice preview view
    private(set) lazy var previewView: PreviewView = {
        let previewView = PreviewView()
        /// drop inferred constraints.
        previewView.translatesAutoresizingMaskIntoConstraints = false
        /// set the camera device to fill the view.
        previewView.videoPreviewLayer.videoGravity = .resizeAspectFill
        return previewView
    }()
    
    /// [GraphicOverlay] offered by the [CameraView] so that graphics can be rendered on top
    /// of the preview images
    public private(set) lazy var graphicOverlay: GraphicOverlay = {
        let overlay = GraphicOverlay()
        /// drop inferred constraints.
        overlay.translatesAutoresizingMaskIntoConstraints = false
        /// let's disable user interaction on the overlay
        overlay.isUserInteractionEnabled = false
        return overlay
    }()
    
    /// CameraSource module that handle the camera device
    private lazy var cameraSource = CameraSource(cameraView: self)
    
    open var analyzer: ImageAnalyzer? {
        get { cameraSource.analyzer }
        set { cameraSource.analyzer = newValue }
    }
        
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

// MARK: - Initialization
private extension CameraView {
    func applyInit() {
        setupPreviewView()
        setupGraphicOverlay()
    }
    
    func setupPreviewView() {
        addSubview(previewView)
        /// properly adjust preview view.
        NSLayoutConstraint.activate([
            previewView.leadingAnchor.constraint(equalTo: leadingAnchor),
            previewView.trailingAnchor.constraint(equalTo: trailingAnchor),
            previewView.topAnchor.constraint(equalTo: topAnchor),
            previewView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
        
        focusRecogninzer = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        // TODO(jansel) - if it remains that GraphicOverlay is a non-user-interactive view then, this is
        //                OK, otherwise we might change isUserInteractionEnabled down below and set GraphicOverlay
        //                as the top level tap receiver.
        previewView.addGestureRecognizer(focusRecogninzer)
    }
    
    func setupGraphicOverlay() {
        addSubview(graphicOverlay)
        /// properly adjust preview view.
        NSLayoutConstraint.activate([
            graphicOverlay.leadingAnchor.constraint(equalTo: leadingAnchor),
            graphicOverlay.trailingAnchor.constraint(equalTo: trailingAnchor),
            graphicOverlay.topAnchor.constraint(equalTo: topAnchor),
            graphicOverlay.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }
}


// MARK: - Recognizers
extension CameraView {
    @objc private func handleTap(_ recognizer: UITapGestureRecognizer) {
        cameraSource.focusAndExpose(at: recognizer.location(in: self))
    }
}

// MARK: - Graphic Overlay
extension CameraView {
    public func addOverlayGraphic(_ view: UIView) {
        view.removeFromSuperview()
        graphicOverlay.addSubview(view)
    }
    
    public func removeOverlayGraphic(_ view: UIView) {
        guard view.superview == graphicOverlay else {
            return
        }
        view.removeFromSuperview()
    }
}

// MARK: - Preview View Coordinate Mapping
/**
 Methods here are offered as a convenient way to translate points and rects from device input
 coordinate system to the coordinate system of the preview view displaying the inputs from the
 camera device. This resamble the convenience translation/scale methods in the android counterpart
 [GraphicOverlay] view.
 */
public extension CameraView {
    /**
     @method previewRect
     @abstract
        Convert a unit rectangle from device input coordinate system to preview layer
        view coordinate system
     
     @discussion See [AVCaptureVideoPreviewLayer.layerRectConverted]
     */
    func previewRect(fromCaptureDeviceRect rect: CGRect) -> CGRect {
        return previewView.videoPreviewLayer.layerRectConverted(fromMetadataOutputRect: rect)
    }
    
    /**
     @method layerRectConverted
     @abstract
        Convert a unit point from device input coordinate system to preview layer
        view coordinate system
     
     @discussion See [AVCaptureVideoPreviewLayer.layerPointConverted]
     */
    func previewPoint(fromCaptureDevicePoint point: CGPoint) -> CGPoint {
        return previewView.videoPreviewLayer.layerPointConverted(fromCaptureDevicePoint: point)
    }
}

// MARK: - Camera Contract
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
