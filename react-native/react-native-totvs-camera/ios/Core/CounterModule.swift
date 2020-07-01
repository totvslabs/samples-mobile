//
//  Counter.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/1/20.
//  Copyright © 2020 TOTVS Lab. All rights reserved.
//

import Foundation

@objc(CounterModule)
class CounterModule : NSObject {
    func increment() {
    }
    
    func getCount(_ callback: RCTResponseSenderBlock) {
        callback([78])
    }
    
    @objc
    func decrement(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any]! {
        return ["initialCount": 0]
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
