//
//  ImageProxyImpl.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/9/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import CoreMedia

/**
* Concrete implementation of [ImageProxy]
*/
class ImageProxyImpl : ImageProxy {
    
    let imageInfo: ImageInfo
    
    var image: CIImage?
    
    var buffer: CMSampleBuffer?
    
    var width: Int
    
    var height: Int
    
    init(image: CIImage, width: Int, height: Int, imageInfo: ImageInfo) {
        self.image = image
        self.buffer = nil
        self.width = width
        self.height = height
        self.imageInfo = imageInfo
    }
    
    init(buffer: CMSampleBuffer, width: Int, height: Int, imageInfo: ImageInfo) {
        self.image = nil
        self.buffer = buffer
        self.width = width
        self.height = height
        self.imageInfo = imageInfo
    }
    
    
    func close() {
        image = nil
        buffer = nil
    }
    
    deinit {
        close()
    }
}
