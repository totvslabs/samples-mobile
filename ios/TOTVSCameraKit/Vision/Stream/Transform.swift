//
//  Transform.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/11/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation
import UIKit

public extension VisionStream {
    /**
     * This is the most general _Intermediate operator_. It transform an upstream into another
     * stream.
     */
    func transform<R>(with transform: @escaping (VisionReceiver<R>, T) -> Void) -> VisionStream<R> {
        return PluggedStream { (upstream: VisionReceiver<R>) -> Connection in
            let connection = self.connect { value in
                transform(upstream, value)
            }
            return connection
        }
    }
    
    /**
     * This is the most general _Intermediate operator_. It transform an upstream into another
     * stream using a [Transformer] interface.
     */
    func transform<R, StreamTransformer: Transformer>(
        with transformer: StreamTransformer
    ) -> VisionStream<R> where StreamTransformer.T == T, StreamTransformer.R == R {
        
        return PluggedStream { (upstream: VisionReceiver<R>) -> Connection in
            let connection = self.connect { value in
                transformer.transform(value: value, receiver: upstream)
            }
            return connection
        }
    }
    
    /**
     * _Intermediate operator_ that cast the elements of upstream to R
     */
    func casted<R>() -> VisionStream<R> {
        return transform { (receiver: VisionReceiver<R>, value: T) in
            receiver.send(value: value as! R)
        } as VisionStream<R>
    }
    
    /**
     * _Intermediate operator_ that filter the elements according to [predicate]
     */
    func filter(where predicate: @escaping (T) -> Bool) -> VisionStream<T> {
        return transform { (receiver: VisionReceiver<T>, value: T) in
            if predicate(value) {
                receiver.send(value: value)
            }
        }
    }
    
    /**
     * _Intermediate operator_ that filter the elements that are instance of [T]
     */
    func filterIsInstance<R>(ofType: R.Type) -> VisionStream<R> {
        return filter(where: { $0 is R }).casted()
    }
    
    /**
     * Returns an stream that map values of type [T] to values of type [R]
     */
    func map<R>(_ mapper: @escaping (T) -> R) -> VisionStream<R> {
        return transform { (receiver: VisionReceiver<R>, value: T) in
            receiver.send(value: mapper(value))
        } as VisionStream<R>
    }
    
    /**
     * Returns an stream that send values received from upstream asynchronously
     * in the provided [DispatchQueue].
     *
     * This allows us to, e.g have an upstream that send it's values under a worker thread
     * but the downstream send's will be executed on [queue]. we can use this to have
     * send operations be performed under the main thread.
     */
    func sendAsync(on queue: DispatchQueue) -> VisionStream<T> {
        return transform { (receiver: VisionReceiver<T>, value: T) in
            queue.async {
                receiver.send(value: value)
            }
        }
    }
}
