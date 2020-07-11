//
//  Transformer.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

/**
* A [Transformer] tries to mimic the same behavior as [transform] _Intermediate operator_
* but giving the ability to have classes implementing a transformation strategy.
*
* This interface is convenient when the operation that would otherwise fit in the [transform]
* block would needs to be stateful or is more convenient to have it on a separate class.
*/
public protocol Transformer {
    associatedtype T
    associatedtype R
    
    /**
     * Receives [value] upstream value and must emit values to
     * downstream receiver
     */
    func transform(value: T, receiver: VisionReceiver<R>)
}
