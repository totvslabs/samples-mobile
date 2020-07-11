//
//  BroadcastVisionStream.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

class BroadcastVisionStream : VisionStream<VisionObject> {
    
    private var receivers = [VisionReceiver<VisionObject>]()
    
    func broadcast(value: VisionObject) {
        receivers.forEach { r in r.send(value: value) }
    }
    
    func add(receiver: VisionReceiver<VisionObject>) {
        receivers.removeAll(where: { $0 === receiver })
        receivers.append(receiver)
    }
    
    func remove(receiver: VisionReceiver<VisionObject>) {
        receivers.removeAll(where: { $0 === receiver })
    }
    
    override func connect(receiver: VisionReceiver<VisionObject>) -> Connection {
        add(receiver: receiver)
        return ReceiverConnection(owner: self, receiver: receiver)
    }
    
    private class ReceiverConnection : Connection {
        weak var owner: BroadcastVisionStream? = nil
        
        let receiver: VisionReceiver<VisionObject>
        
        init(owner: BroadcastVisionStream, receiver: VisionReceiver<VisionObject>) {
            self.owner = owner
            self.receiver = receiver
        }
        
        func dicsonnect() {
            owner?.remove(receiver: receiver)
        }
    }
}
