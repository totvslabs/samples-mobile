package com.totvs.camera

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
     * Set target rotation for the camera preview
     */
    var rotation: Int

    /**
     * Set facing of the camera device
     */
    var facing: LensFacing

    /**
     * Set zoom for the camera
     */
    var zoom: Float

    /**
     * Trigger the action of capturing a picture.
     * @TODO as right now is unclear what to do with the captured image, this
     *       callback is only intended for notifications
     */
    public fun takePicture(onTaken: OnPictureTakenCallback)
}