//
//  VisionFaceCameraswift.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
* [VisionCamera] dedicated to face detection/recognition
*/
public protocol VisionFaceCamera : VisionCamera {
    /**
     * RGB to be used as color for overlay graphics on this camera view.
     */
    var overlayGraphicsColor: String { get set }
    
    /**
     * Configure a liveness strategy to use in this vision camera.
     */
    var liveness: Liveness? { get set }
    
    /**
     * Face proximity feature
     */
    var proximity: Proximity? { get set }
    
    /**
     * Whether or not this face vision camera has a liveness feature installed
     */
    var  isLivenessEnabled: Bool { get }
    
    /**
     * Whether or not this face vision camera has a proximity feature installed
     */
    var isFaceProximityEnabled: Bool { get }
    
    /**
     * Setup this [VisionFaceCamera] with a proper model and options
     */
    func setup(model: RecognitionDetectionModel<String, Face>)
    
    /**
     * Capture and perform recognition task on this face vision camera view.
     *
     * @param options control certain aspects of the recognition process.
     * @param onResult callback called once the recognition task is completed.
     */
    func recognizeStillPicture(options: RecognitionOptions, onResult: (RecognitionResult) -> Void)
}
