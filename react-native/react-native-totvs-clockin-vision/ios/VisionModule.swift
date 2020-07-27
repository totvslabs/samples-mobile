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
    
    @objc func getModelOutputDirectory(
        _ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock
    ) {
        resolve("<random path from ios vision lib>")
    }
}
