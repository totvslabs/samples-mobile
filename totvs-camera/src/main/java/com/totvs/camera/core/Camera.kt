package com.totvs.camera.core

/**
 * Representation of a camera device. Operations here are the one performed
 * with either an standard camera device or with outputs of such device.
 *
 * @note @TODO Operations regarding to real-time processing will be added later
 */
public interface Camera {
    /**
     * Weather or not to enabled torch light for this camera
     */
    var isTorchEnabled: Boolean

    /**
     * Set facing of the camera device
     */
    var facing: LensFacing

    /**
     * Set zoom for the camera
     */
    var zoom: Float

    /**
     * Toggle camera facing, works the same as requesting the opposite of the current
     * facing.
     */
    public fun toggleCamera()

    /**
     * Trigger a capture image action.
     *
     * This method automatically save the captured in the location specified on
     * options and then call the callback
     */
    public fun takePicture(options: OutputFileOptions, onSaved: OnImageSaved)

    /**
     * Trigger a capture image action.
     *
     * This method don't save the captured image but instead hand the image as a proxy
     * to the caller.
     *
     * IMPORTANT: the caller must close the captured image after using it, otherwise
     * no more images might be taken and handled down to the caller. This behavior
     * is implementation dependent, but is mandatory to close the captured image.
     */
    public fun takePicture(onCaptured: OnImageCaptured)
}