//
//  Connection.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
 * [Connection] represent the connection of a receiver to an stream. It can be
 * used to disconnect from the upstream stream.
 *
 * It's recommended to disconnect from upstream after being done with it.
 */
public protocol Connection {
    /**
     * Trigger to the upstream a disconnect token
     */
    func dicsonnect()
}


/**
 * [Connection] that ignore [disconnect] tokens
 */
class IgnoreConnection : Connection {
    func dicsonnect() { }
}
