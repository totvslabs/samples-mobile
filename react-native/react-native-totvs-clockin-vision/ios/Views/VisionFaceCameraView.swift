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
    private lazy var faceGraphic = FaceGraphic(cameraView: self)
    
    /**
     * Whether or not we're on a ready state.
     */
    private var isReady = false
    
    /**
     * Flag to determine when the camera is busy processing one image for recognition.
     */
    private var isRecognizing = false
    
    /**
     * Handy getter to get the real installed analyzer.
     */
    private var detectorAnalyzer: DetectionAnalyzer? {
        analyzer as? DetectionAnalyzer
    }
            
    /// - [FaceVisionCamera] contract
    var overlayGraphicsColor: String = "#FFFFFF" {
        didSet {
            faceGraphic.setLandmarksColor(rgb: overlayGraphicsColor)
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
    }
    
    open override func didMoveToSuperview() {
        startUp()
    }
}

/// MARK: Pseudo Camera contract.
/// View JS Properties. This is a pseudo contract because it forward to real Camera contract
/// properties.
extension VisionFaceCameraView {
    /**
     This properties are exposed to JS as part of the view component properties.
     They serve the same purpose of setter in the ViewManager on android.
     */
    
    @objc func setFacing(_ value: NSNumber) {
        if let facing = CameraFacing(rawValue: Int(truncating: value)) {
            self.facing = facing
        }
    }
    
    @objc func setZoom(_ value: NSNumber) {
        zoom = Float(truncating: value)
    }
}

/// MARK: Debugging
private extension VisionFaceCameraView {
    func enableDebug() {
        isDebug = true
    }
}

/// MARK: Lifecycle
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
        detectionQueue.resume()
        recognitionQueue.resume()
    }
    
    /**
     * Suspend task processing on all queues.
     */
    func tearDownDispatchQueues() {
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

/// MARK: Liveness
private extension VisionFaceCameraView {
    /**
     * Enable the liveness feature in this vision camera view
     */
    func enable(liveness: Liveness) {
        if isDebug {
            print("\(TAG): Enabling liveness \(liveness). Analyzer is ready: \(nil != analyzer)")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()
        
        // closing current connection
        livenessConnection?.disconnect()
        
        // setup the vision stream for face detected objects.
        livenessConnection = detectorAnalyzer?
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .connect(liveness)
        
        installFaceGraphics(forLiveness: liveness)
    }
}

/// MARK: Proximity
private extension VisionFaceCameraView {
    /**
     * Enable the proximity feature on this vision camera view.
     */
    func enable(proximity: Proximity) {
        if isDebug {
            print("\(TAG): Enabling proximity \(proximity). Analyzer is ready: \(nil != analyzer)")
        }
        // we install the analyzer as a pre-requisite
        installAnalyzer()

        // closing current connection
        proximityConnection?.disconnect()
        
        // setup the vision stream for face detected objects.
        proximityConnection = detectorAnalyzer?
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .connect(proximity)
    }
}

/// MARK: FaceGraphic
private extension VisionFaceCameraView {
    /**
     * Enable graphic overlay on the camera
     */
    func installFaceGraphics(forLiveness liveness: Liveness) {
        if isDebug {
            print("\(TAG): Enabling face graphics. Analyzer is ready: \(nil != analyzer)")
        }
        
        // clear the face graphics.
        faceGraphic.clear()
        // clear every object on the graphic overlay
        graphicOverlay.clear()
        // re-add the face graphic overlay
        graphicOverlay.add(faceGraphic.view)
        // closing current connection
        graphicsConnection?.disconnect()
        
        faceGraphic.drawEyes = liveness is LivenessEyes
        faceGraphic.drawNose = liveness is LivenessFace
        
        graphicsConnection = detectorAnalyzer?
            .detections
            .filterIsInstance(ofType: FaceObject.self)
            .sendAsync(on: DispatchQueue.main)
            .connect(faceGraphic)
    }
}

/// MARK: DetectionAnalyzer
private extension VisionFaceCameraView {
    /**
     * Install detection analyzer on [CameraView]
     */
    func installAnalyzer() {
        guard nil != analyzer else {
            return
        }
        analyzer = DetectionAnalyzer(
            queue: detectionQueue,
            detectors: FaceDetector()
        )
    }
    
    /**
     * Check if we need to uninstall the analyzer
     */
    func checkAnalyzerState() {
        if nil == liveness && nil == proximity {
            detectorAnalyzer?.disableDetector(withKey: FaceDetector.key)
            analyzer = nil
        }
    }
}

/// MARK: VisionFaceCamera contract
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
    
    private func recognizeImage(image: UIImage, onRecognize: ([Face], Error?) -> Void) {
        do {
            try model?.recognize(input: image, onRecognized: { faces in
                onRecognize(faces, nil)
            })
        } catch let error {
            print("\(TAG): Error recognizing: \(error)")
            onRecognize([], error)
        }
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
     */
    func recognizeStillPicture(options: RecognitionOptions, onResult: (RecognitionResult) -> Void) {
        ensureSetup { }
        
        if isRecognizing {
            if isDebug {
                print("\(TAG): Recognizer is busy, ignoring this request.")
            }
            return
        }
        isRecognizing = true
        
        takePicture { (image, error) in
            if let error = error {
                self.isRecognizing = false
                print("\(TAG): Error taking picture: \(error)")
                return
            }
            if let uiImage = image?.image {
                // we add an extra task if we're required to save the image.
                let latch = CountDownLatch(count: 1 + (options.saveImage ? 1 : 0))
                var result = RecognitionResult()

                // image saver
                if (options.saveImage) {
                    self.recognitionQueue.async {
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
                self.recognitionQueue.async {
                    self.recognizeImage(image: uiImage) { faces, error in
                        if let error = error {
                            print("\(TAG): Error recognizing image: \(error)")
                        }
                        result.faces = faces
                        // notify we're done
                        latch.countDown()
                    }
                }
                // wait till the tasks are done
                latch.wait()
                self.isRecognizing = false
                
                onResult(result)
            }
            // close properly the proxy
            image?.close()
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
