//
//  ImageProxyImpl.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/9/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

/**
* Concrete implementation of [ImageProxy]
*/
class ImageProxyImpl : ImageProxy {
    
    private(set )var buffer: CVImageBuffer?
    
    let imageInfo: ImageInfo
    
    var image: CGImage? {
        get {
            guard let buffer = buffer else {
                return nil
            }
            let context = CIContext()
            let image = CIImage(cvPixelBuffer: buffer)
            return context.createCGImage(image, from: image.extent)
        }
    }
    
    var cropRect: CGRect
    
    var width: Float  { Float(cropRect.width)  }
    
    var height: Float { Float(cropRect.height) }
    
    init(buffer: CVImageBuffer?, imageRect: CGRect, imageInfo: ImageInfo) {
        self.buffer = buffer
        self.imageInfo = imageInfo
        self.cropRect = imageRect
    }
    
    func close() {
        buffer = nil
    }
}
