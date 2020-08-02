//
//  CapturePictureProcessor.swift
//  totvs-camera-app
//
//  Created by Jansel Rodriguez on 6/29/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import AVFoundation
import Photos
import UIKit

class CaptureProcessor : NSObject {
        
    private(set) var photoSettings: AVCapturePhotoSettings
    
    private let options: OutputFileOptions
    private let savePhoto: Bool
    private let onComplete: (CaptureProcessor) -> Void
    private let onImage: (UIImage?, Error?) -> Void
    private let onSave: (URL?, Error?) -> Void
    private let desiredImageSize: CGSize?
    
    private var photoData: Data?
    
    init(with settings: AVCapturePhotoSettings,
         options: OutputFileOptions,
         savePhoto: Bool = true,
         desiredImageSize: CGSize? = nil,
         onImage: @escaping (UIImage?, Error?) -> Void = { _, _ in },
         onSave: @escaping (URL?, Error?) -> Void = { _, _ in },
         onComplete: @escaping (CaptureProcessor) -> Void = { _ in }
         )
    {
        self.photoSettings = settings
        self.options = options
        self.savePhoto = savePhoto
        self.desiredImageSize = desiredImageSize
        self.onImage = onImage
        self.onSave = onSave
        self.onComplete = onComplete
    }
    
    private func didFinish() {
        onComplete(self)
    }
}

/// MARK: AVCapturePhotoCaptureDelegate
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
            if let dir = options.outputDirectory {
                saveImageToDirectory(photoData, inDir: dir)
            } else {
                saveImageToLibrary(photoData)
            }
        } else {
            onImage(photoData.compressedImage(atDesiredSize: desiredImageSize), nil)
            didFinish()
        }
    }
}

/// MARK: Image Saving
private extension CaptureProcessor {
    private func saveImageToLibrary(_ data: Data) {
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
                    self.onSave(nil, error)
                    self.didFinish()
                })
            } else {
                self.didFinish()
            }
        }
    }
    
    private func saveImageToDirectory(_ data: Data, inDir dir: URL) {
        let path = createFile(outputDirectory: dir)
        do {
            try data
                .compressedImageData(atDesiredSize: desiredImageSize)
                .write(to: path, options: .atomicWrite)
            
            onSave(path, nil)
        } catch let error {
            print("Error trying to save image to \(path): \(error)")
            onSave(nil, error)
        }
        didFinish()
    }
        
    private func createFile(outputDirectory: URL, extension: String = "jpg") -> URL {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMddHHmmss"
        formatter.locale = Locale.init(identifier: "en_US_POSIX")
        
        return outputDirectory
            .appendingPathComponent(formatter.string(from: Date()))
            .appendingPathExtension(`extension`)
    }
}

fileprivate extension UIImage {
    func resized(atDesiredSize desiredSize: CGSize) -> UIImage? {
        let percentage = desiredSize.width / self.size.width
        let isOpaque = true
        let canvas = CGSize(width: size.width * percentage, height: size.height * percentage)
        let format = imageRendererFormat
        format.opaque = isOpaque
        return UIGraphicsImageRenderer(size: canvas, format: format).image {
            _ in draw(in: CGRect(origin: .zero, size: canvas))
        }
    }
}
fileprivate extension Data {
    // returns an compressed image data if possible, self otherwise.
    func compressedImageData(atDesiredSize desiredSize: CGSize?) -> Data {
        // possibly compress the image down.
        guard let desiredSize = desiredSize, let image = UIImage(data: self)!.resized(atDesiredSize: desiredSize) else {
            return self
        }
        return image.jpegData(compressionQuality: 1.0)!
    }
    
    
    func compressedImage(atDesiredSize desiredSize: CGSize?) -> UIImage {
        let image = UIImage(data: self)!
        
        guard let desiredSize = desiredSize, let compressed = image.resized(atDesiredSize: desiredSize) else {
            return image
        }
        return compressed
    }
}
