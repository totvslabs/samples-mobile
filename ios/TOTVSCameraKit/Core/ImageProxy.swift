//
//  ImageProxy.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit
import CoreMedia

/**
 A public proxy with the same interface as `CGImage`.
 
 This allow us to offer a unified and provided interface of an image
 that is independent of the provider of captured images.
 */
public protocol ImageProxy {
    
    /**
     @method close:
     @abstract
        Close this resource. This will close the underlying `image` field.
     
     @discussion
        Failure to close the proxy after use might lead to not receiving more images.
        It's mandatory to close this image after use.
     */
    func close()
    
    /**
     @property image
     @abstract
        Raw image for a single capture.
     
     @discussion
        Since this is a proxy around an image, is possible that over time
        we wrap here another representation of image, hence this might return null
        under those conditions.            
     */
    var image: CIImage? { get }

    /**
     @property buffer
     @abstract
        Raw buffer for a single capture.
     
     @discussion
        Since this is a proxy around an image buffer, is possible that over time
        we wrap here another representation of image buffer, hence this might return null
        under those conditions.
     
        This is preffered over [image]
     */
    var buffer: CMSampleBuffer? { get }
        
    /**
     @property width
     @abstract
        Returns the image width.
     */
    var width: Int { get }

    /**
     @property height
     @abstract
        Returns the image height.
     */
    var height: Int { get }
    
    /**
     @property imageInfo
     @abstract
        Returns the info of this image.
     */
    var imageInfo: ImageInfo { get }
}

/// MARK: Use handy extension
public extension ImageProxy {
    func use(_ block: (ImageProxy) throws -> Void) {
        defer {
            close()
        }
        do {
            try block(self)
        } catch {
        }
    }
}
