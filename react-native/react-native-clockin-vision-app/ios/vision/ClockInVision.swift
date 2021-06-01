//
//  ClockInVision.swift
//  ReactNativeTotvsClockinVisionApp
//
//  Created by Jansel Rodriguez on 8/5/20.
//

import Foundation
import react_native_totvs_clockin_vision

@objcMembers
class ClockinVision : NSObject {
  
  private static let model = NativeRecognitionModel()
  
  static func setup() {
    ModelProvider.setFaceRecognitionDetectionModel(model: model)
  }
}
