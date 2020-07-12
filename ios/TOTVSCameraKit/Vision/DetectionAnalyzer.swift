//
//  DetectionAnalyzer.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
* Immutable detection analyzer that keep a registry of [VisionDetector]. By default detectors
* are enabled until they're manually disabled.
*
* This analyzer models a set of detectors that have the potential to be all enabled at the
* same time or only some of them.
*
* An enabled detector is one for which on every run of [analyze] it executes it detection
* procedure.
*
* [VisionDetector] can be enabled and disabled at any time through [enable] and [disable]
* methods. These operations do nothing if the requested [VisionDetector] doesn't exists
* in this [DetectionAnalyzer].
*
* This analyzer post individual detection tasks on [executor] which means that if the
* executor can ony run less than the number of detectors passed to this analyzer, the
* executor will halt until previous detection finishes. It's highly recommended that the
* executor capacity is at least as the number of detectors passed to this analyzer.
*
* This analyzer closes the [ImageProxy] after all the detectors are done with it.
*
* Contract [VisionDetector] on this analyzer must meet:
*
* 1. By nature this analyzer expect the detectors to emit only one detection, thus is a
* requirement on the detector to identify the appropriate measure to detect the most
* prominent detection and report it. The behavior is undefined if multiple detections
* are emitted by a single detection phase.
*
* For cases in which multiple emission are allowed from the detectors on a single phase
* one can subclass this analyzer and override [analyze] then the detectors can post
* a kind of [VisionObject] that pack multiple results and having the new analyzer
* coordinate the detectors emissions.
*
* 2. [VisionDetector] on this analyzer are required to call [onDetected] callback so the
* analyzer trigger that they are done performing their work. If this is not performed
* this analyzer will lock until the [executor] then is shut down.
*/
open class DetectionAnalyzer {
    private let queue: DispatchQueue
    
    /**
     * Registry of all detectors
     */
    private var registry = [AnyHashable: EnabledDetector]()
    
    /**
     * [VisionStream] of detection determined by this analyzer.
     *
     * This stream represents the flow of objects detected from all the enabled
     * [VisionDetector] in this analyzer.
     */
    public private(set) var detections: VisionStream<VisionObject> = BroadcastVisionStream()
    
    /**
     * We skip frames while we're in the middle of a detection task
     */
    private var isBusy = false
    
    
    public init(queue: DispatchQueue, detectors: VisionDetector...) {
        self.queue = queue
        detectors.forEach { detector in
            registry[detector.instanceKey] = EnabledDetector(detector: detector)
        }
    }
    
    /**
     * Returns whether the detector for the [key] is enabled or not
     */
    open func isDetectorEnabled(withKey key: AnyHashable) -> Bool {
        return registry[key]?.enabled ?? false
    }
    
    /**
     * Enable the detector corresponding to [key]
     */
    open func enableDetector(withKey key: AnyHashable) {
        registry[key]?.enabled = true
    }
    
    /**
     * Disable the detector corresponding to [key]
     */
    open func disableDetector(withKey key: AnyHashable) {
        registry[key]?.enabled = false
    }
    
    /**
     * Get the detector corresponding to [key]
     */
    open subscript(_ key: AnyHashable) -> VisionDetector? {
        return registry[key]?.detector
    }
    
    /**
     * After detection let's post this object to the stream
     */
    private func post(value: VisionObject) {
        (detections as! BroadcastVisionStream).broadcast(value: value)
    }
        
    private class EnabledDetector {
        weak var detector: VisionDetector? = nil
        var enabled: Bool = true
        
        init(detector: VisionDetector) {
            self.detector = detector
        }
    }
    
    deinit {
        registry.removeAll()
    }
}

// MARK: - ImageAnalyzer
extension DetectionAnalyzer : ImageAnalyzer {
    open func analyze(image: ImageProxy) {
        if !isBusy {
            isBusy = true
            
            image.use { _ in
                let detectors = registry.values.filter({ d in d.enabled })
                
                // let's synchronize the detectors to know when we can analyze another frame
                let semaphore = DispatchSemaphore(value: detectors.count)
                
                detectors.forEach { it in
                    queue.async {
                        it.detector?.detect(on: self.queue, image: image) { [weak self] value in
                            semaphore.signal()
                            self?.post(value: value) // let's post the detected value to the stream
                        }
                    }
                }
                semaphore.wait()
                // all detectors are done, let's set to analyze more images
                isBusy = false
            }
        }
    }
}
