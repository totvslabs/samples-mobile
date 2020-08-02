//
//  Camera.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

/**
 * Representation of a camera device. Operations here are the one performed
 * with either an standard camera device or with outputs of such device.
 *
 * This is a test modification
 */
public protocol Camera {
    /**
     @property isTorchEnabled
     @abstract
        Weather or not to enabled torch light for this camera
     */
    var isTorchEnabled: Bool { get set }
    
    /**
     @property facing
     @abstract
        Set facing of the camera device
     */
    var facing: CameraFacing { get set }
    
    /**
     @property zoom
     @abstract
        Set zoom for the camera
     
     @discussion
        Default value is 0.0 indicating no zoom.
     */
    var zoom: Float { get set }
    
    /**
     @method toggleCamera:
     @abstract
        Toggle camera facing.
     
     @discussion
        This method works the same as requesting the opposite of the current facing.
     */
    func toggleCamera()
    
    /**
     @method takePicture(options:onSaved:):
     @abstract
        Trigger a capture image action.
     
     @discussion
        This method automatically save the captured in the location specified on
        options and then call the callback.
     
        If options.outputDirectory is nil then the image is saved to the photo library.
     */
    func takePicture(with options: OutputFileOptions, onSaved: @escaping OnImageSaved)
    
    /**
     @method takePicture(onCaptured:):
     @abstract
        Trigger a capture image action.
     
     @discussion
        This method don't save the captured image but instead hand the image as a proxy
        to the caller.
    
        IMPORTANT: the caller must close the captured image after using it, otherwise
        no more images might be taken and handled down to the caller. This behavior
        is implementation dependent, but is mandatory to close the captured image.
     */
    func takePicture(_ onCaptured: @escaping OnImageCaptured)
}
