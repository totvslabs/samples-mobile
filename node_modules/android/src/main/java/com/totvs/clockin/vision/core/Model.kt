package com.totvs.clockin.vision.core

import androidx.annotation.WorkerThread

/**
 * Model as representative of model classes that are capable of being trained and released.
 *
 * @see [RecognitionModel] and [DetectionModel]
 */
interface Model {

    /**
     * Indicate whether or not this model is already trained.
     */
    val isTrained: Boolean
    /**
     * Initialize the model accordingly. After the call to this method
     * the model might be ready to perform other operations on its inputs.
     * If this is the case then [isTrained] might return true after a call to this method.
     *
     * Is advisable to call [train] and not rely on this method to have the model
     * trained. Is up to the implementation if [train] will have any effect after
     * calling this method.
     */
    @WorkerThread
    fun init()

    /**
     * Train the model and leave it ready for further operations.
     *
     * @see [RecognitionModel] and [DetectionModel]
     */
    @WorkerThread
    fun train()

    /**
     * Release the trained model. It's important to release the model after no longer needed
     * because this might incur in memory release.
     */
    @WorkerThread
    fun release()

    /**
     * Configuration of the model
     */
    data class Config(val modelDirectory: String)
}