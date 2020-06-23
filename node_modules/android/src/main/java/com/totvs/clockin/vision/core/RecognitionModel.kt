package com.totvs.clockin.vision.core

import androidx.annotation.WorkerThread

/**
 * Model as representative of model classes that are capable of recognition
 * tasks over an input value.
 */
interface RecognitionModel<Input, Output> : Model {
    /**
     * Perform recognition over the input producing a list of recognized entities of
     * type [Output].
     *
     * This method might throw [IllegalStateException] if called before being the model
     * trained. This is implementation detail
     */
    @WorkerThread
    @Throws(IllegalStateException::class)
    fun recognize(input: Input, onRecognized: (List<Output>) -> Unit)
}