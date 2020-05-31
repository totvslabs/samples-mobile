package com.totvs.camera.vision

import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.face.FaceDetector
import com.totvs.camera.vision.barcode.BarcodeDetector

/**
 * Interface for detectors. The nature of a detectors can be of _Single emission_ or
 * _Multiple emission_ during it detection phase.
 *
 * A _Single emission_ [VisionDetector] tries to detect the entities in an [ImageProxy]
 * and then take the most prominent detection and report it as the suited on while a
 * _Multiple emission_ [VisionDetector] reports all the detected entities.
 *
 * Most if not all the detectors in this library are _Single emission_ detectors.
 * Each detector have a prominent selection strategy that determines how the detector
 * select it's most prominent detection.
 *
 * @see [FaceDetector] and [BarcodeDetector]
 */
interface VisionDetector<T : VisionObject> {
    /**
     * Key of this detector
     */
    val key: Key<*>

    /**
     * Run detection on [image] and report the result back on [onDetected]
     */
    fun detect(image: ImageProxy, onDetected: (T) -> Unit)

    /**
     * Detector key
     */
    interface Key<T : VisionDetector<*>>
}