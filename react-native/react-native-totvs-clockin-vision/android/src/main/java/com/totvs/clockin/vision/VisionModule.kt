package com.totvs.clockin.vision

import androidx.annotation.AnyThread
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.totvs.clockin.vision.core.Model.Config
import com.totvs.clockin.vision.core.ModelProvider
import com.totvs.clockin.vision.utils.*
import kotlin.concurrent.thread

/**
 * Module representative of this library. It performs and expose an interface
 * through which the app can perform some utility and handy tasks that are not tied
 * to any of the views exposed in this library.
 */
class VisionModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    override fun getName() = NAME

    // START Module methods

    /**
     * Set the name of the output and location directory for the recognition model
     */
    @AnyThread
    @ReactMethod
    fun setModelOutputDirectoryName(name: String) = setModelDirName(name)

    /**
     * Get the location of the model output directory.
     */
    @AnyThread
    @ReactMethod
    fun getModelOutputDirectory(promise: Promise) = promise.resolve(getModelOutputDir())

    /**
     * Create the model output and captures output directories
     */
    @AnyThread
    @ReactMethod
    fun setupModelDirectories(promise: Promise) = prepareModelDirectories(context) { success ->
        promise.resolve(success)
    }

    /**
     * Utility to trigger the recognition model training operation
     */
    @AnyThread
    @ReactMethod
    fun trainRecognitionModel(promise: Promise) {
        thread {
            ModelProvider.getFaceRecognitionModel(
                Config(
                    modelDirectory = getModelOutputDir()
                )
            ).train()
            promise.resolve(true)
        }
    }

    /**
     * Retrieve the base64 representation of any image located at [path]
     */
    @AnyThread
    @ReactMethod
    fun getImageFileBase64(path: String?, promise: Promise) =
        promise.resolve(path?.let { getFileBase64(path) })

    /**
     * Delete the image located at [path]
     */
    @AnyThread
    @ReactMethod
    fun deleteImageFile(path: String?, promise: Promise) =
        promise.resolve(path?.let { deleteFile(path) })

    // END Module methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionModule"
    }
}