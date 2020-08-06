//
//  ModelConfigswift.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

/**
 * Configuration of the model
 */
public struct ModelConfig {
    public let modelDirectory: String
    
    public init(modelDirectory: String) {
        self.modelDirectory = modelDirectory
    }
}
