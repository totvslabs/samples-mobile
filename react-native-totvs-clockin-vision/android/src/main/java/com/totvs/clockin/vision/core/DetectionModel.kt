package com.totvs.clockin.vision.core

import androidx.annotation.WorkerThread

/**
 * Model as representative of model classes that are capable of detection
 * tasks over an input value.
 */
interface DetectionModel<Input, Output> :
    Model {
    /**
     * Perform detection over the input producing a list of detected entities of
     * type [Output].
     *
     * This method might throw [IllegalStateException] if called before being the model
     * trained. This is implementation detail
     */
    @WorkerThread
    @Throws(IllegalStateException::class)
    fun detect(input: Input, onDetected: (List<Output>) -> Unit)
}