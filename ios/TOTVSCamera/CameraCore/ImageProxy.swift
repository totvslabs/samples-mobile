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
     
        Notice that [image] must not be closed by the caller, instead close this proxy.
     */
    var image: CGImage? { get }
}
