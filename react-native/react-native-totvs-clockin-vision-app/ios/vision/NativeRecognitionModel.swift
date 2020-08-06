//
//  NativeRecognitionModel.swift
//  ReactNativeTotvsClockinVisionApp
//
//  Created by Jansel Rodriguez on 8/4/20.
//

import Foundation
import UIKit
import react_native_totvs_clockin_vision

fileprivate let TAG = "NativeRecognitionModel"

class NativeRecognitionModel : RecognitionDetectionModel<UIImage, Face> {
  
  private let model = ObjcFaceRecognizer()
  private var config: ModelConfig? = nil
  
  public override init() {
    super.init()
  }
  
  override func configure(with config: ModelConfig) {
    self.config = config    
  }

  override func train() {
    guard let config = config else {
      fatalError("\(TAG): No ModelConfig registered, did you forget to call config(with:) ?")
    }
    print("\(TAG): Training native face recognizer...")
    
    model.setup(config.modelDirectory)
    
    print("\(TAG): Native face recognizer trained!")
  }
  
  override func detect(input: UIImage, onDetected: @escaping ([Face]) -> Void) throws {
    onDetected([])
  }
  
  override func recognize(input image: UIImage, onRecognized: @escaping ([Face]) -> Void) throws {
    let base64 = image.toBase64()
    let json = model.recognizeFaces(base64).data(using: .utf8)!
    
    let result = try JSONDecoder().decode(RecognitionResult.self, from: json)
      
    onRecognized(result.results)
  }
}

fileprivate extension UIImage {
  func toBase64() -> String {
    return pngData()!.base64EncodedString(options: .lineLength64Characters)
  }
}

/**
 Classes representing the native recognition library return protocol
 */
fileprivate enum RecognitionStatus : String {
  case faceDetected = "FaceDetected"
  case faceNotDetected = "FaceNotDetected"
  case multipleFaceDetected = "MultipleFacesDetected"
}

fileprivate struct RecognitionResult : Decodable {
  let status: String
  let results: [NativeFace]
}

fileprivate struct NativeFace : Decodable, Face {
    
  enum CodingKeys: String, CodingKey {
    case name, distance
    case personId = "person_id"
  }
  
  let name: String
  let personId: String
  let distance: Float
  let encoding: String = ""
}
