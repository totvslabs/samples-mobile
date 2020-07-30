//
//  NativeFaceModel.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

class NativeFaceModel : RecognitionDetectionModel<String, Face> {
    static let `default` = NativeFaceModel()
    
    private override init() {
    }
}
