//
//  NativeFace.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/30/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation


struct NativeFace : Face {
    var label: String? = nil
    
    let confidence: Float = 0.0
    
    var boundingBox: CGRect = .zero
    
    var encoding: String? = nil
}
