//
//  VisionModule.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/27/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation


@objc(VisionModule)
class VisionModule : NSObject {
    
    @objc class func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    /**
     * Set the name of the output and location directory for the recognition model
     */
    @objc func setModelOutputDirectoryName(_ name: NSString) {
        setModelDirName(name: String(name))
    }
       
    /**
     * Get the location of the model output directory.
     */
    @objc func getModelOutputDirectory(_
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        resolve(NSString(string: getModelOutputDir()))
    }
    
    /**
     * Create the model output and captures output directories
     */
    @objc func setupModelDirectories(_
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        prepareModelDirectories { success in
            resolve(success)
        }
    }
    
    /**
     * Utility to trigger the recognition model training operation
     */
    @objc func trainRecognitionModel(_
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        DispatchQueue.global().async {
            ModelProvider.getFaceRecognitionDetectionModel(
                config: ModelConfig(
                    modelDirectory: getModelOutputDir()
                )
            ).train()
            
            resolve(true)
        }
    }
    
    /**
     * Retrieve the base64 representation of any image located at [path]
     */
    @objc func getImageFileBase64(_ path: NSString,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        resolve(getFileBase64(atPath: String(path)))
    }
    
    /**
     * Delete the image located at [path]
     */
    @objc func deleteImageFile(_ path: NSString,
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock)
    {
        resolve(deleteFile(atPath: String(path)))
    }
}
