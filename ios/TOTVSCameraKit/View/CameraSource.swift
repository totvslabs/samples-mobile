//
//  CameraSource.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import AVFoundation
import UIKit

fileprivate let TAG = "CameraSource"

fileprivate enum SessionSetupState {
    case configured
    case undetermined
}

fileprivate let SUPPORT_ANALYSIS = true
/**
 * This controller is backed fully by `AVFoudnation` implementation, in order to change the
 * source of the camera device, this is the class that needs to be checked for.
 */
class CameraSource : NSObject {
    /// Dispatch queue for camera operations
    private let sessionQueue  = DispatchQueue(label: "CameraView")
    /// Dispatch queue for camera data output
    private let analysisQueue = DispatchQueue(label: "ImageAnalysis")
        
    /// `CameraView` holding the device preview
    private weak var cameraView: CameraView!
    
    /// `PreviewView` owner of the camera device layer
    private var previewView: PreviewView {
        cameraView.previewView
    }
     
    /// Capture Session for the current camera device
    private let session = AVCaptureSession()
    
    /// Capture Device input
    @objc private dynamic var cameraDeviceInput: AVCaptureDeviceInput!
    
    /// Capture Output
    private let photoOutput = AVCapturePhotoOutput()
    
    /// Analysis Data Output
    private let dataOutput = AVCaptureVideoDataOutput()
    
    /// Flag that indicate if the current session is running
    private var isSessionRunning = false
    
    /// State of the session configuration
    private var sessionSetupState : SessionSetupState = .undetermined
    
    private let deviceDiscovery = AVCaptureDevice.DiscoverySession(
        deviceTypes: [.builtInWideAngleCamera, .builtInDualCamera, .builtInTrueDepthCamera],
        mediaType: .video,
        position: .unspecified
    )
    
    // KVO observations
    private var kvoObservations = [NSKeyValueObservation]()
    
    /// Underlaying camera lens facing
    private var underlayingFacing: CameraFacing = .back
    /// Camera current facing
    internal var facing: CameraFacing {
        get { underlayingFacing }
        set {
            if underlayingFacing == newValue {
                return
            }
            underlayingFacing = newValue
            if sessionSetupState != .undetermined {
                changeCamera(facing: underlayingFacing)
            }
        }
    }
    
    private var inProgressCaptures = [Int64: CaptureProcessor]()
            
    init(cameraView: CameraView) {
        self.cameraView = cameraView
    }
}


/// MARK: Lifecycle
extension CameraSource {
    func startRunning() {
        previewView.session = session
        
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            if sessionSetupState == .undetermined {
                sessionQueue.async {
                    self.configureSession()
                }
            }
            sessionQueue.async {
                self.addObservers()
                self.session.startRunning()
                self.isSessionRunning = self.session.isRunning
            }
        case .notDetermined:
            requestCameraAccess()
        default: break
        }
    }
    /**
     * The user has not yet been presented with the option to grant
     * video access. Suspend the session queue to delay session
     * setup until the access request has completed.
     
     * Note that audio access will be implicitly requested when we
     * create an AVCaptureDeviceInput for audio during session setup.
     */
    private func requestCameraAccess() {
        sessionQueue.suspend()
        AVCaptureDevice.requestAccess(for: .video, completionHandler: { [weak self] granted in
            self?.sessionQueue.resume()
            
            if granted {
                self?.startRunning()
            }
        })
    }
    
    func stopRunning() {
        if (sessionSetupState == .configured) {
            sessionQueue.async {
                self.removeObservers()
                self.session.stopRunning()
                self.isSessionRunning = self.session.isRunning
            }
        }
    }
}

/// MARK: Session Management
private extension CameraSource {
    func configureSession() {
        guard sessionSetupState == .undetermined else {
            return
        }
        session.beginConfiguration()
        /**
         Do not create an AVCaptureMovieFileOutput when setting up the session because
         Live Photo is not supported when AVCCaptureMovieFileOutput is added to the session.
         */
        session.sessionPreset = .photo
            
        /// - TAG: CameraDeviceInput
        do {
            guard let cameraDevice = bestCameraDevice(forFacing: facing) else {
                print("\(TAG): Default camera device is unavailable.")
                session.commitConfiguration()
                return
            }
            let cameraInput = try AVCaptureDeviceInput(device: cameraDevice)
            
            if session.canAddInput(cameraInput) {
                session.addInput(cameraInput)
                cameraDeviceInput = cameraInput
            } else {
                print("\(TAG): Couldn't add video device input to the session.")
                session.commitConfiguration()
                return
            }
        } catch {
            print("\(TAG): Couldn't create video device input: \(error)")
            session.commitConfiguration()
            return
        }
        
        /// - TAG: PhotoCaptureOutput
        if session.canAddOutput(photoOutput) {
            session.addOutput(photoOutput)
            
            photoOutput.isHighResolutionCaptureEnabled = true
            photoOutput.maxPhotoQualityPrioritization = .balanced
        } else {
            print("\(TAG): Couldn't add photo output to session")
            session.commitConfiguration()
            return
        }
        
        if SUPPORT_ANALYSIS {
            if session.canAddOutput(dataOutput) {
                session.addOutput(dataOutput)
                dataOutput.setSampleBufferDelegate(self, queue: analysisQueue)
            } else {
                print("\(TAG): Couldn't add data output to session")
                session.commitConfiguration()
                return
            }
        }
        
        // initial orientation to all interested
        DispatchQueue.main.async {
            self.setCameraOrientation(orientation: self.cameraView.windowOrientation)
        }
        
        session.commitConfiguration()
        
        sessionSetupState = .configured
    }
    
    func bestCameraDevice(forFacing facing: CameraFacing) -> AVCaptureDevice? {
        let position = facing.toPosition()
        let devices = deviceDiscovery.devices
        return devices.first(where: { $0.position == position })
    }
    
    func setCameraOrientation(orientation: UIInterfaceOrientation) {
        var videoOrientation: AVCaptureVideoOrientation = .portrait
            
        if orientation != .unknown {
            if let orientation = AVCaptureVideoOrientation(interfaceOrientation: orientation) {
                videoOrientation = orientation
            }
        }
        // change preview orientation
        previewView.videoPreviewLayer.connection?.videoOrientation = videoOrientation
        // change image data output orientation
        sessionQueue.async {
            if let dataConnection = self.dataOutput.connection(with: .video) {
                dataConnection.videoOrientation = videoOrientation
            }
        }
    }
    
    func setCameraOrientation(orientation: UIDeviceOrientation) {
        if let connection = previewView.videoPreviewLayer.connection {
            guard let deviceOrientation = AVCaptureVideoOrientation(deviceOrientation: orientation),
                orientation.isPortrait || orientation.isLandscape else {
                return 
            }
            // change preview orientation
            connection.videoOrientation = deviceOrientation
            
            // change image data output orientation
            sessionQueue.async {
                if let dataConnection = self.dataOutput.connection(with: .video) {
                    dataConnection.videoOrientation = deviceOrientation
                }
            }
        }        
    }
}

/// MARK: Observer Interruption / KVO and Notifications
private extension CameraSource {
    func addObservers() {
        observeSessionStatus()
        observeSystemPressure()
        observeFocusChange()
        observeRuntimeError()
        observeInterrupptions()
        observeOrientationChanges()
    }
    
    func removeObservers() {
        NotificationCenter.default.removeObserver(self)
        
        for o in kvoObservations {
            o.invalidate()
        }
        kvoObservations.removeAll()
    }
    
    /// - TAG: Sssion State
    func observeSessionStatus() {
        let observation = session.observe(\.isRunning, options: .new) { _, change in
        }
        kvoObservations.append(observation)
    }
    
    /// - TAG: System Pressure
    /**
     * If the device is experiencing some pressure such as overheating, the camera won't degrade quality or
     * drop frames on its own; If it reaches a critical point it will stops working. We adjust frame rates
     * accordingly
     */
    func observeSystemPressure() {
        let observation = observe(\.cameraDeviceInput.device.systemPressureState, options: .new) { [weak self] _, change in
            guard let systemPressureState = change.newValue else {
                return
            }
            self?.adjustFrameRateFor(systemPressureState: systemPressureState)
        }
        kvoObservations.append(observation)
    }
    
    /// - TAG: Subject Area Focus Changes Observations
    func observeFocusChange() {
        let center = NotificationCenter.default
        center.addObserver(self,
                           selector: #selector(subjectAreaDidChange),
                           name: .AVCaptureDeviceSubjectAreaDidChange,
                           object: cameraDeviceInput.device)
    }
    
    /// - TAG: Runtime Error Observations
    func observeRuntimeError() {
        let center = NotificationCenter.default
        center.addObserver(self,
                           selector: #selector(sessionRuntimeError),
                           name: .AVCaptureSessionRuntimeError,
                           object: cameraDeviceInput.device)
    }
    /// - TAG: Interruption Observations
    func observeInterrupptions() {
        let center = NotificationCenter.default
        center.addObserver(self,
                           selector: #selector(sessionWasInterrupted),
                           name: .AVCaptureSessionWasInterrupted,
                           object: cameraDeviceInput.device)
        
        center.addObserver(self,
                           selector: #selector(sessionInterruptionEnded),
                           name: .AVCaptureSessionInterruptionEnded,
                           object: cameraDeviceInput.device)
    }
    /// - TAG: Orientation Changes
    func observeOrientationChanges() {
        let center = NotificationCenter.default
        center.addObserver(self,
                           selector: #selector(adjustVideoOrientation),
                           name: UIDevice.orientationDidChangeNotification,
                           object: nil)
    }
}

/// MARK: Focus Changes
extension CameraSource {
    
    @objc private func subjectAreaDidChange(notification: NSNotification) {
        let devicePoint = CGPoint(x: 0.5, y: 0.5)
        focus(with: .continuousAutoFocus, exposureMode: .continuousAutoExposure, at: devicePoint, monitorSubjectAreaChange: false)
    }
    
    func focusAndExpose(at point: CGPoint) {
        let devicePoint = previewView.videoPreviewLayer.captureDevicePointConverted(fromLayerPoint: point)
        focus(with: .autoFocus, exposureMode: .autoExpose, at: devicePoint, monitorSubjectAreaChange: true)
    }
    
    private func focus(
        with focusMode: AVCaptureDevice.FocusMode,
        exposureMode: AVCaptureDevice.ExposureMode,
        at devicePoint: CGPoint,
        monitorSubjectAreaChange: Bool)
    {
        sessionQueue.async {
            let device = self.cameraDeviceInput.device
            do {
                try device.lockForConfiguration()
                
                /*
                 Setting (focus/exposure)PointOfInterest alone does not initiate a (focus/exposure) operation.
                 Call set(Focus/Exposure)Mode() to apply the new point of interest.
                 */
                if device.isFocusPointOfInterestSupported && device.isFocusModeSupported(focusMode) {
                    device.focusPointOfInterest = devicePoint
                    device.focusMode = focusMode
                }
                
                if device.isExposurePointOfInterestSupported && device.isExposureModeSupported(exposureMode) {
                    device.exposurePointOfInterest = devicePoint
                    device.exposureMode = exposureMode
                }
                
                device.isSubjectAreaChangeMonitoringEnabled = monitorSubjectAreaChange
                device.unlockForConfiguration()
            } catch {
                print("Could not lock device for configuration: \(error)")
            }
        }
    }
}

/// MARK: Handle Runtime Error
private extension CameraSource {
    @objc func sessionRuntimeError(notification: NSNotification) {
        guard let error = notification.userInfo?[AVCaptureSessionErrorKey] as? AVError else {
            return
        }
        print("\(TAG): Capture session runtime error \(error)")
        // If media service were reset nd the last start succeeded, restart the session
        if error.code == .mediaServicesWereReset {
            if isSessionRunning {
                sessionQueue.async {
                    self.session.startRunning()
                    self.isSessionRunning = self.session.isRunning
                }
            }
        }
    }
}

/// MARK: Handle Interruptionss
/**
 * A session can only run when the app is still full screen. It will be interrupted
 * in a multi-app layout, introduced in iOS 9, see also the documentation of
 * AVCaptureSessionInterruptionReason. Add observers to handle these session interruptions
 * and show a preview is paused message. See the documentation of AVAVCaptureSessionWasInterruptedNotification
 * for other interruption reasons.
 */
private extension CameraSource {
    @objc func sessionWasInterrupted(notification: NSNotification) {
        if let info = notification.userInfo?[AVCaptureSessionInterruptionReasonKey] as AnyObject?,
           let reasonInteger = info.integerValue,
           let reason = AVCaptureSession.InterruptionReason(rawValue: reasonInteger)
        {
            print("\(TAG): Capture Session was interrupted with reason \(reason)")
            if reason == .videoDeviceNotAvailableDueToSystemPressure {
                print("\(TAG): Session stopped running due to shutdown system pressure level.")
            }
        }
    }
    
    @objc func sessionInterruptionEnded(notification: NSNotification) {
        print("\(TAG): Capture session interruption ended")
        sessionQueue.async {
            self.session.startRunning()
            self.isSessionRunning = self.session.isRunning
        }
    }
}

/// MARK: Handle System Pressure
private extension CameraSource {
    func adjustFrameRateFor(systemPressureState: AVCaptureDevice.SystemPressureState) {
        let pressureLevel = systemPressureState.level
        
        if pressureLevel == .serious || pressureLevel == .critical {
            do {
                try cameraDeviceInput.device.lockForConfiguration()
                print("WARNING: Reached elevated system pressure level: \(pressureLevel). Throttling frame rate.")
                cameraDeviceInput.device.activeVideoMinFrameDuration = CMTime(value: 1, timescale: 20)
                cameraDeviceInput.device.activeVideoMaxFrameDuration = CMTime(value: 1, timescale: 15)
                cameraDeviceInput.device.unlockForConfiguration()
            } catch {
                print("\(TAG): Could not lock device for configuration \(error)")
            }
        } else if pressureLevel == .shutdown {
            print("\(TAG): Session stopped working due to shutdown system pressure level.")
        }
    }
}

private extension CameraSource {
    @objc func adjustVideoOrientation(notification: NSNotification) {
        setCameraOrientation(orientation: UIDevice.current.orientation)
    }
}

/// MARK: Camera Position
extension CameraSource {
    func toggleCamera() {
        if facing == .back {
            facing = .front
        } else {
            facing = .back
        }
    }
    
    private func changeCamera(facing: CameraFacing) {
        sessionQueue.async {
            let currentCameraDevice = self.cameraDeviceInput.device
            let currentPosition = currentCameraDevice.position
            let requestPosition = facing.toPosition()
            // must not happens but just to guarantee
            guard currentPosition != requestPosition else {
                return
            }
            
            let newCameraDevice = self.deviceDiscovery.devices.first(where: { $0.position == requestPosition })
            if let cameraDevice = newCameraDevice {
                do {
                    let cameraDeviceInput = try AVCaptureDeviceInput(device: cameraDevice)
                    self.session.beginConfiguration()
                    
                    // Remove the existing device input first, because AVCaptureSession doesn't support
                    // simultaneous use of the rear and front cameras.
                    self.session.removeInput(self.cameraDeviceInput)
                    if self.session.canAddInput(cameraDeviceInput) {
                        // re-attach subject area chnages observations
                        let center = NotificationCenter.default
                        center.removeObserver(self, name: .AVCaptureDeviceSubjectAreaDidChange, object: currentCameraDevice)
                        center.addObserver(self, selector: #selector(self.subjectAreaDidChange), name: .AVCaptureDeviceSubjectAreaDidChange, object: cameraDeviceInput.device)
                        
                        self.session.addInput(cameraDeviceInput)
                        self.cameraDeviceInput = cameraDeviceInput
                    } else {
                        self.session.addInput(self.cameraDeviceInput)
                    }
                    // reconfigure the outputs
                    self.photoOutput.maxPhotoQualityPrioritization = .balanced
                    
                    self.session.commitConfiguration()
                } catch {
                    print("\(TAG): Error occurred while creating video device input: \(error)")
                }
            }
        }
    }
}

/// MARK: Torch & Zoom
extension CameraSource {
    var isTorchEnabled: Bool {
        get {
            sessionSetupState == .configured && cameraDeviceInput.device.torchMode == .on
        }
        set {
            setCameraTorch(enabled: newValue)
        }
    }
    
    /// Set zoom value, ppossible values are [0, 1]
    var zoom: Float {
        get {
            if sessionSetupState == .undetermined {
                return 0.0
            }
            return Float(cameraDeviceInput.device.videoZoomFactor)
        }
        set {
            setZoom(to: CGFloat(newValue))
        }
    }
    
    private func setCameraTorch(enabled: Bool) {
        sessionQueue.async {
            do {
                guard self.sessionSetupState == .configured else { return }
                let device = self.cameraDeviceInput.device
                
                if enabled && device.torchMode == .on ||
                  !enabled && device.torchMode == .off
                {
                    return
                }
                try device.lockForConfiguration()
                device.torchMode = enabled ? .on : .off
                device.unlockForConfiguration()
            } catch { }
        }
    }
    
    private func setZoom(to factor: CGFloat) {
        sessionQueue.async {
            do {
                guard self.sessionSetupState == .configured else { return }
                let device = self.cameraDeviceInput.device
                
                let minFactor = device.minAvailableVideoZoomFactor
                // relax the zoom a little bit.
                let maxFactor = device.maxAvailableVideoZoomFactor > 20 ? 10 : device.maxAvailableVideoZoomFactor
                let zoom = minFactor + (maxFactor - minFactor) * factor
                                
                try device.lockForConfiguration()
                device.videoZoomFactor = zoom
                device.unlockForConfiguration()
            } catch { }
        }
    }
}

/// MARK: Captures
extension CameraSource {
    func takePicture(_ onCaptured: @escaping OnImageCaptured) {
    }
    
    func takePicture(with options: OutputFileOptions, onSaved: @escaping OnImageSaved) {
        let previewOrientation = previewView.videoPreviewLayer.connection?.videoOrientation
        sessionQueue.async {
            if let photoConnection = self.photoOutput.connection(with: .video) {
                photoConnection.videoOrientation = previewOrientation!
            }
            var photoSettings = AVCapturePhotoSettings()
            
            // Capture HEIF photos when supported. Enable auto-flash and high-resolution photos.
            if self.photoOutput.availablePhotoCodecTypes.contains(.hevc) {
                photoSettings = AVCapturePhotoSettings(format: [AVVideoCodecKey: AVVideoCodecType.hevc])
            }
            if self.cameraDeviceInput.device.isFlashAvailable {
                photoSettings.flashMode = .auto
            }
            photoSettings.isHighResolutionPhotoEnabled = true
            if !photoSettings.__availablePreviewPhotoPixelFormatTypes.isEmpty {
                photoSettings.previewPhotoFormat = [kCVPixelBufferPixelFormatTypeKey as String: photoSettings.__availablePreviewPhotoPixelFormatTypes.first!]
            }
            let processor = CaptureProcessor(
                with: photoSettings,
                savePhoto: true,
                onSave: { _ in
                    onSaved(nil, nil)
                }, onComplete: { processor in
                    self.sessionQueue.async {
                        self.inProgressCaptures[processor.photoSettings.uniqueID] = nil
                    }
                })
            // The photo output holds a weak reference to the photo capture delegate and stores it in an array to maintain a strong reference.
            self.inProgressCaptures[photoSettings.uniqueID] = processor
            self.photoOutput.capturePhoto(with: photoSettings, delegate: processor)
        }
    }
}

/// MARK: Image Data Analysis
extension CameraSource : AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        if let connection = output.connection(with: .video) {
            print("Receiving images: orientation: \(connection.videoOrientation.rawValue)")
        }
    }
}

fileprivate extension AVCaptureVideoOrientation {
    init?(deviceOrientation: UIDeviceOrientation) {
        switch deviceOrientation {
        case .portrait: self = .portrait
        case .portraitUpsideDown: self = .portraitUpsideDown
        case .landscapeLeft: self = .landscapeRight
        case .landscapeRight: self = .landscapeLeft
        default: return nil
        }
    }
    
    init?(interfaceOrientation: UIInterfaceOrientation) {
        switch interfaceOrientation {
        case .portrait: self = .portrait
        case .portraitUpsideDown: self = .portraitUpsideDown
        case .landscapeLeft: self = .landscapeLeft
        case .landscapeRight: self = .landscapeRight
        default: return nil
        }
    }
}

fileprivate extension CameraFacing {
    func toPosition() -> AVCaptureDevice.Position {
        return self == .back ? .back : .front
    }
}
