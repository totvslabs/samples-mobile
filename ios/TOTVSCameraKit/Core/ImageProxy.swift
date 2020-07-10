//
//  ImageProxy.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

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
     
        We recommend to use [buffer] for analysis instead, since accesing the underlying CGImage
        requires a creation of such image from the buffer. It might be more optimized to use
        the buffer directly.    
     */
    var image: CGImage? { get }
    
    /**
     @property buffer
     @abstract
        Raw image buffer for a single capture.
     
     @discussion
        Since this is a proxy around an image buffer, is possible that over time
        we wrap here another representation of image buffer, hence this might return null
        under those conditions.
     
         We recommend to use [buffer] for analysis instead, since accesing the underlying CGImage
         requires a creation of such image from the buffer. It might be more optimized to use
         the buffer directly.
     */
    var buffer: CVImageBuffer? { get }
    
    /**
     @property cropRect
     @abstract
        Get/Set the crop rectangle of the image.
     
     @discussion
        This can be set to mirror the image size
     */
    var cropRect: CGRect  { get }
    
    /**
     @property width
     @abstract
        Returns the image width.
     */
    var width: Float { get }

    /**
     @property height
     @abstract
        Returns the image height.
     */
    var height: Float { get }
    
    /**
     @property imageInfo
     @abstract
        Returns the info of this image.
     */
    var imageInfo: ImageInfo { get }
}

/// MARK: Use handy extension
public extension ImageProxy {
    func use<R>(_ block: (ImageProxy) throws -> R) -> R? {
        defer {
            close()
        }
        do {
            return try block(self)
        } catch {
        }
        return nil
    }
}
