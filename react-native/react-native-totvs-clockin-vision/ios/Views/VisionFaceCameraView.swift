//
//  VisionFaceCameraView.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import TOTVSCameraKit

fileprivate var isDebug = false
fileprivate var TAG  = "FaceVisionCameraView"
/**
* Camera View capable of face detection.
*/
@objc(VisionFaceCameraView)
class VisionFaceCameraView : CameraView, VisionFaceCamera {
    /**
     * DispatchQueue for detection tasks.
     */
    private let detectionQueue = DispatchQueue(label: "DetectionQueue")
    
    /**
     * DispatchQueue for recognition tasks
     */
    private let recognitionQueue = DispatchQueue(label: "RecognitionQueue")

    /**
     * Special DispatchQueue only to be meant to process recognition results.
     * It was found out that the CountDownLatch bloc the whole queue keeping
     * the tasks submited to it to not execute.
     */
    private let recognitionProcessingQueue = DispatchQueue(label: "RecognitionProcessingQueue")
    /**
     * Model to be used for recognition
     */
    private var model: RecognitionDetectionModel<UIImage, Face>? = nil
    
    /**
     * [Connection] of liveness feature
     */
    private var livenessConnection: Connection? = nil
    
    /**
     * [Connection] of proximity feature
     */
    private var proximityConnection: Connection? = nil
    
    /**
     * [Connection] of face graphics
     */
    private var graphicsConnection: Connection? = nil
    
    /**
     * Graphic to record graphics.
     */
    private var faceGraphic: FaceGraphic?
    
    /**
     * [Transformer] that scale nose landmark from the source coordinate into [GraphicOverlay]
     * coordinate system.
     */
    private var faceNoseTranslator: FaceNoseTranslator?
    
    /**
     * Whether or not we're on a ready state.
     */
    private var isReady = false
    
    /**
     Whether the dispatch queues are ready to process tasks. i.e not resumed
     */
    private var areQueueProcessing = true
    
    /**
     * Flag to determine when the camera is busy processing one image for recognition.
     */
    private var isRecognizing = false
        
    /**
     * Strong reference to detection analyzer since [CameraView] holds a weak one.
     */
    private lazy var detectorAnalyzer = DetectionAnalyzer(queue: detectionQueue, detectors: FaceDetector.default)
            
    /// - [FaceVisionCamera] contract
    var overlayGraphicsColor: String = "#FFFFFFFF" {
        didSet {
            faceGraphic?.setLandmarksColor(rgb: overlayGraphicsColor)
        }
    }
    
    var liveness: Liveness? = nil {
        didSet {
            // if we got a liveness installed then we enable the detector, otherwise we
            // check if we need to uninstall it.
            guard let liveness = liveness else {
                checkAnalyzerState()
                return
            }
            enable(liveness: liveness)
        }
    }
    
    var proximity: Proximity? = nil {
        didSet {
            // if we got a proximity installed then we enable the detector, otherwise we
            // don't event enable the detector.
            guard let proximity = proximity else {
                checkAnalyzerState()
                return
            }
            enable(proximity: proximity)
        }
    }
    
    var isLivenessEnabled: Bool {
        nil != liveness
    }
    
    var isFaceProximityEnabled: Bool {
        proximity != nil
    }
    
    /// - Events
    private var onLiveness: OnLiveness? = nil
    private var onFaceProximity: OnFaceProximity? = nil
    private var onFaceRecognized: OnFaceRecognized? = nil
    
    /// - Transient State variables
    
    // required blinks for the liveness eye mode.
    private var requiredBlinks = 0
    // threshold for the proximity feature.
    private var proximityThreshold = Float(0.0)
        
    override init(frame: CGRect) {
        super.init(frame: frame)
        enableDebug()
        startUp()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        enableDebug()
        startUp()
    }

    open override func removeFromSuperview() {
        tearDown()
        super.removeFromSuperview()        
    }
    
    open override func didMoveToSuperview() {
        super.didMoveToSuperview()
        startUp()
    }
}

// MARK: - Pseudo Camera contract.

/// View JS Properties & Events. This is a pseudo contract because it forward to real Camera contract
/// properties.
extension VisionFaceCameraView {
    /**
     This properties are exposed to JS as part of the view component properties.
     They serve the same purpose of setter in the ViewManager on android.
     */
    
    
    /// - JS View Properties
    ///
    /**
     * Set initial camera facing
     */
    @objc func setFacing(_ value: NSNumber) {
        if let facing = CameraFacing(rawValue: Int(truncating: value)) {
            self.facing = facing
        }
    }
    
    /**
     * Set initial camera zoom
     */
    @objc func setZoom(_ value: NSNumber) {
        zoom = Float(truncating: value)
    }
    
    /**
     * Sets the appropriate liveness mode
     */
    @objc func setLivenessMode(_ livenessMode: NSNumber) {
        switch livenessMode.intValue {
        case LivenessEyes.id:
            liveness = LivenessEyes(requiredBlinks: requiredBlinks) { [weak self] result in
                // on detection send the event
                self?.sendLivenessEvent(with: result)
            }
            break
        case LivenessFace.id:
            liveness = LivenessFace { [weak self] result in
                // on detection send the event
                self?.sendLivenessEvent(with: result)
            }
            break
        default: liveness = nil // disable the liveness
        }
    }
    
    /**
     * This property is only related to liveness eyes and control the number of blinks
     * to track until regarding a face as live.
     */
    @objc func setLivenessBlinkCount(_ blinksCount: NSNumber) {
        requiredBlinks = blinksCount.intValue
        
        (liveness as? LivenessEyes)?.requiredBlinks = requiredBlinks
    }
    
    /**
     * Set a default color for overlay graphics. This is used to control the
     * color with which landmarks are colored under liveness feature.
     */
    @objc func setOverlayGraphicsColor(_ rgb: NSString) {
        overlayGraphicsColor = String(rgb)
    }
    
    /**
     * Set the proximity detector
     */
    @objc func setIsProximityEnabled(_ enabled: NSNumber) {
        guard enabled.boolValue else {
            proximity = nil
            return
        }
        if let proximity = self.proximity as? ProximityByFaceWidth {
            proximity.threshold = proximityThreshold
        } else {
            proximity = ProximityByFaceWidth(threshold: proximityThreshold) { [weak self] result in
                // on detection send the event
                self?.sendProximityEvent(with: result)
            }
        }
    }
    
    /**
     * Sets the appropriate proximity threshold value
     */
    @objc func setProximityThreshold(_ threshold: NSNumber) {
        proximityThreshold = threshold.floatValue
        
        (proximity as? ProximityByFaceWidth)?.threshold = proximityThreshold
    }
    
    /// - JS View Events
    @objc func setOnLiveness(_ event: RCTDirectEventBlock?) {
        onLiveness = OnLiveness(emit: event)
    }
        
    @objc func setOnFaceProximity(_ event: RCTDirectEventBlock?) {
        onFaceProximity = OnFaceProximity(emit: event)
    }
    
    @objc func setOnFaceRecognized(_ event: RCTDirectEventBlock?) {
        onFaceRecognized = OnFaceRecognized(emit: event)
    }
}

// MARK: - Events
internal extension VisionFaceCameraView {
    func sendLivenessEvent(with result: LivenessResult) {
        onLiveness?.send(data: result)
    }
    
    func sendProximityEvent(with result: ProximityResult) {
        onFaceProximity?.send(data: result)
    }
    
    func sendFaceRecognitionEvent(with result: RecognitionResult) {
        onFaceRecognized?.send(data: result)
    }
}

// MARK: - Debugging
private extension VisionFaceCameraView {
    func enableDebug() {
        isDebug = true
    }
}

// MARK: - Lifecycle
private extension VisionFaceCameraView {
    /**
     * Setup every requirement of this face vision camera
     */
    func startUp() {
        // let's optimize for capture/saving
        desiredOutputImageSize = CGSize(width: 594, height: 1056)
        
        guard !isReady else {
            return
        }
        if isDebug {
            print("\(TAG): Setting up camera requirements")
        }
        setupDispatchQueues()
        isReady = true
    }
    
    /**
     * Turn down every requirement of this face vision camera
     */
    func tearDown() {
        isReady = false
        
        if isDebug {
            print("\(TAG): Tear down camera requirements")
        }
        tearDownDispatchQueues()
        closeConnections()
    }
    
    /**
     * Setup the dispatch queues for background jobs
     */
    func setupDispatchQueues() {
        guard !areQueueProcessing else {
            return
        }
        areQueueProcessing = true
        
        detectionQueue.resume()
        recognitionQueue.resume()
    }
    
    /**
     * Suspend task processing on all queues.
     */
    func tearDownDispatchQueues() {
        areQueueProcessing = false
        
        detectionQueue.suspend()
        recognitionQueue.suspend()
    }
    
    /**
     * Disconnect all [VisionStream] connections
     */
    func closeConnections() {
        livenessConnection?.disconnect()
        proximityConnection?.disconnect()
        graphicsConnection?.disconnect()
    }
}

// MARK: - Liveness
private extension VisionFaceCameraView {
    /**
     * Enable the liveness feature in this vision camera view
     */
    func enable(liveness: Liveness) {
        if isDebug {
            print("\(TAG): Enabling liveness \(liveness). Had valid analyzer before?: \(nil != analyzer)")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()
        
        // closing current connection
        livenessConnection?.disconnect()
        
        faceNoseTranslator = liveness is LivenessFace ? FaceNoseTranslator(cameraView: self) : nil
        
        // setup the vision stream for face detected objects.
        if liveness is LivenessFace {
            livenessConnection = detectorAnalyzer
                .detections
                .filterIsInstance(ofType: FaceObject.self)
                .sendAsync(on: .main) // translator uses cameraView
                .transform(with: faceNoseTranslator!)
                .connect(liveness)
        } else {
            livenessConnection = detectorAnalyzer
                .detections
                .filterIsInstance(ofType: FaceObject.self)
                .connect(liveness)
        }
        
        installFaceGraphics(forLiveness: liveness)
    }
    
    // Turn down liveness connections and graphics
    func disableLiveness() {
        if isDebug {
            print("\(TAG): Disabling liveness")
        }
        livenessConnection?.disconnect()
        // uninstall all face graphics
        clearFaceGraphics()
    }
}

// MARK: - Proximity
private extension VisionFaceCameraView {
    /**
     * Enable the proximity feature on this vision camera view.
     */
    func enable(proximity: Proximity) {
        if isDebug {
            print("\(TAG): Enabling proximity \(proximity). Had valid analyzer before?: \(nil != analyzer)")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()

        // closing current connection
        proximityConnection?.disconnect()
        
        // setup the vision stream for face detected objects.
        proximityConnection = detectorAnalyzer
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .connect(proximity)
    }
    
    // Turn down proximity connections
    func disableProximity() {
        if isDebug {
            print("\(TAG): Disabling proximity")
        }
        proximityConnection?.disconnect()
    }
}

// MARK: - FaceGraphic
private extension VisionFaceCameraView {
    /**
     * Enable graphic overlay on the camera
     */
    func installFaceGraphics(forLiveness liveness: Liveness) {
        if isDebug {
            print("\(TAG): Enabling face graphics.")
        }
        // closing current connection
        graphicsConnection?.disconnect()
        
        clearFaceGraphics()
        
        faceGraphic = FaceGraphic(cameraView: self)
        // re-add the face graphic overlay
        graphicOverlay.add(faceGraphic!.view)
        
        faceGraphic!.drawEyes = liveness is LivenessEyes
        faceGraphic!.drawNose = liveness is LivenessFace
        
        graphicsConnection = detectorAnalyzer
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .sendAsync(on: .main)
            .connect(faceGraphic!)
    }
    
    /**
     * Clear face graphics and remove the graphic from overlay
     */
    func clearFaceGraphics() {
        // clear the face graphics.
        faceGraphic?.clear()
        // clear every object on the graphic overlay
        graphicOverlay.clear()
    }
}

// MARK: - DetectionAnalyzer
private extension VisionFaceCameraView {
    /**
     * Install detection analyzer on [CameraView]
     */
    func installAnalyzer() {
        guard nil == analyzer else {
            return
        }
        if isDebug {
            print("\(TAG): Installing DetectionAnalyzer...")
        }
        detectorAnalyzer.enableDetector(withKey: FaceDetector.key)
        
        analyzer = detectorAnalyzer
    }
    
    /**
     * Check if we need to uninstall the analyzer
     */
    func checkAnalyzerState() {
        if nil == liveness {
            // disconnect liveness connections
            disableLiveness()
        }
        if nil == proximity {
            // disconnnect proximity connections
            disableProximity()
        }
        
        if nil == liveness && nil == proximity {
            detectorAnalyzer.disableDetector(withKey: FaceDetector.key)
            analyzer = nil
        }
    }
}

// MARK: - VisionFaceCamera contract
extension VisionFaceCameraView {
    private func ensureSetup(block: () -> Void) {
        guard let _ = model else {
            fatalError("\(TAG) haven't been setup. Please call setup first")
        }
        return block()
    }
    
    private func saveImage(image: UIImage, options: RecognitionOptions, onSave: (URL?, Error?) -> Void) {
        guard let dir = options.outputDir else {
            fatalError("No possible to save the image, please check options.outputDir")
        }
        let file = createFile(outputDirectory: dir)
        do {
            try image.jpegData(compressionQuality: 1.0)!.write(to: file, options: .atomicWrite)
            
            onSave(file, nil)
        } catch let error {
            print("\(TAG): Error saving image at \(file): \(error)")
            onSave(nil, error)
        }
    }
    
    private func recognizeImage(image: UIImage, onRecognize: @escaping (ModelOutput<Face>, Error?) -> Void) {
        do {
            try model?.recognize(input: image, onRecognized: { faces in
                onRecognize(faces, nil)
            })
        } catch let error {
            print("\(TAG): Error recognizing: \(error)")
            onRecognize(.empty(), error)
        }
    }
    
    private func processPhoto(on image: ImageProxy?, error: Error?, options: RecognitionOptions, onResult: @escaping (RecognitionResult) -> Void) {
        if let error = error {
            isRecognizing = false
            print("\(TAG): Error taking picture: \(error)")
            return
        }
        if let uiImage = image?.image {
            // we add an extra task if we're required to save the image.
            let latch = CountDownLatch(count: 1 + (options.saveImage ? 1 : 0))
            var result = RecognitionResult()

            // image saver
            if (options.saveImage) {
                recognitionQueue.async {
                    self.saveImage(image: uiImage, options: options) { url, error in
                        if let error = error {
                            print("\(TAG): Error saving image: \(error)")
                        }
                        result.imagePath = url
                        // notify we're done
                        latch.countDown()
                    }
                }
            }
            // image recognizer
            recognitionQueue.async {
                self.recognizeImage(image: uiImage) { output, error in
                    if let error = error {
                        print("\(TAG): Error recognizing image: \(error)")
                    }
                    result.output = output
                    // notify we're done
                    latch.countDown()
                }
            }
            // wait till the tasks are done
            latch.wait()
            isRecognizing = false
            
            onResult(result)
        }
        // close properly the proxy
        image?.close()
    }
        
    /**
     * Let's setup the view with an appropriate model and options
     */
    func setup(model: RecognitionDetectionModel<UIImage, Face>) {
        self.model = model
    }
    
    /**
     * Capture an still picture and recognizes the person on it.
     *
     * [options] is used to control the behavior of the recognition task. if [options.saveImage]
     * is set to true we'll save the picture at [options.outputDir] otherwise we'll skip that
     * task.
     *
     * Unlike the android counterpart, this recognition doesn't suppport detection metadata and relies on the
     * c++ lib to do detection
     */
    func recognizeStillPicture(options: RecognitionOptions, onResult: @escaping (RecognitionResult) -> Void) {
        ensureSetup { }
        
        if isRecognizing {
            if isDebug {
                print("\(TAG): Recognizer is busy, ignoring this request.")
            }
            return
        }
        isRecognizing = true
        
        takePicture { (image, error) in
            self.recognitionProcessingQueue.async {
                self.processPhoto(on: image, error: error, options: options, onResult: onResult)
            }
        }
    }
}

// Utility to create a file under [outputDirectory]
fileprivate func createFile(outputDirectory: URL, extension: String = "jpg") -> URL {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyyMMddHHmmss"
    formatter.locale = Locale.init(identifier: "en_US_POSIX")
    
    return outputDirectory
        .appendingPathComponent(formatter.string(from: Date()))
        .appendingPathExtension(`extension`)
}
