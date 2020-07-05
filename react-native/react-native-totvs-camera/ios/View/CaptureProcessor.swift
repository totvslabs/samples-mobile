//
//  CapturePictureProcessor.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/29/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import AVFoundation
import Photos

class CaptureProcessor : NSObject {
    
    private let savePhoto: Bool
    
    private(set) var photoSettings: AVCapturePhotoSettings
    
    private let onComplete: (CaptureProcessor) -> Void
    private let onData: (Data?) -> Void
    private let onSave: (URL?) -> Void
    
    private var photoData: Data?
    
    init(with settings: AVCapturePhotoSettings,
         savePhoto: Bool = true,
         onData: @escaping (Data?) -> Void = { _ in },
         onSave: @escaping (URL?) -> Void = { _ in },
         onComplete: @escaping (CaptureProcessor) -> Void = { _ in }
         )
    {
        self.photoSettings = settings
        self.savePhoto = savePhoto
        self.onData = onData
        self.onSave = onSave
        self.onComplete = onComplete
    }
    
    private func didFinish() {
        onComplete(self)
    }
}

extension CaptureProcessor : AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        if let error = error {
            print("Error capturing photo \(error)")
        } else {
            photoData = photo.fileDataRepresentation()
        }
    }
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishCaptureFor resolvedSettings: AVCaptureResolvedPhotoSettings, error: Error?) {
        if let error = error {
            print("Error capturing photo \(error)")
            didFinish()
            return
        }
        guard let photoData = photoData else {
            print("No photo data resource")
            didFinish()
            return
        }
        if savePhoto {
            savePhoto(photoData)
        }
    }
    
    private func savePhoto(_ data: Data) {
        PHPhotoLibrary.requestAuthorization { status in
            if status == .authorized {
                PHPhotoLibrary.shared().performChanges({
                    let options = PHAssetResourceCreationOptions()
                    let request = PHAssetCreationRequest.forAsset()
                    options.uniformTypeIdentifier = self.photoSettings.processedFileType.map { $0.rawValue }
                    request.addResource(with: .photo, data: data, options: options)
                    
                }, completionHandler: { _, error in
                    if let error = error {
                        print("Error occurred while saving photo to photo library: \(error)")
                    }
                    self.onSave(nil)
                    self.didFinish()
                })
            } else {
                self.didFinish()
            }
        }
    }
}
