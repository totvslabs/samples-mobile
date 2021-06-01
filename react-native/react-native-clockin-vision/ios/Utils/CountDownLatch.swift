//
//  CountDownLatch.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/2/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
 A simple implementation of a latch
 */
class CountDownLatch {
    private var count: Int32
    private let semaphore = DispatchSemaphore(value: 0) // initially locked
    
    init(count: Int32) {
        precondition(count > 0, "count must be greather than 0")
        self.count = count
    }
    
    func countDown() {
        OSAtomicDecrement32(&count)
        
        if count <= 0 {
            semaphore.signal() // unlock
        }
    }
    
    func wait() {
        semaphore.wait()
    }
}
