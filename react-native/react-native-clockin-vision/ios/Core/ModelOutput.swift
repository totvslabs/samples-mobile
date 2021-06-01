//
//  ModelOutput.swift
//  TOTVSClockInVision
//
//  Created by Jansel Valentin on 1/11/21.
//

import Foundation

/**
 * Interface to express the result of models installed in this libvrary.
 */
open class ModelOutput<T> {
    /**
     * List of entities recognized/detected by the model.
     */
    public let entities: [T]
    
    /**
     * Status of the operation, can be anything relevant to the consumer.
     */
    public let status: String
    
    /**
     * Encoding information about the operation, can be anything relevant to the consumer
     */
    public let encoding: String
    
    public init(status: String, encoding: String, entities: [T]) {
        self.status = status
        self.encoding = encoding
        self.entities = entities
    }
    
    public static func empty<T>() -> ModelOutput<T> {
        return ModelOutput<T>(status: "", encoding: "", entities: [])
    }
    
    public static func with<T>(status: String = "", encoding: String = "", entities: [T] = []) -> ModelOutput<T> {
        return ModelOutput<T>(status: status, encoding: encoding, entities: entities)
    }
}
