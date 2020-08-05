//
//  NativeRecognitionModel.swift
//  ReactNativeTotvsClockinVisionApp
//
//  Created by Jansel Rodriguez on 8/4/20.
//

import Foundation
import UIKit
import react_native_totvs_clockin_vision

class NativeRecognitionModel : RecognitionDetectionModel<UIImage, Face> {
  
  private static let model = ObjcFaceRecognizer()
  
  public override init() {
    super.init()
  }
  
  override func configure(with: ModelConfig) {
  }
  
  override func initialize() {
  }
  
  override func train() {
  }
  
  override func detect(input: UIImage, onDetected: @escaping ([Face]) -> Void) throws {
    onDetected([])
  }
  
  override func recognize(input: UIImage, onRecognized: @escaping ([Face]) -> Void) throws {
  }
}
