package com.totvs.clockin.vision

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.totvs.clockin.vision.core.Model
import com.totvs.clockin.vision.core.Model.Config
import com.totvs.clockin.vision.core.ModelProvider
import com.totvs.clockin.vision.utils.getModelOutputDir
import com.totvs.clockin.vision.utils.prepareModelDirectories
import com.totvs.clockin.vision.utils.setModelDirName
import kotlin.concurrent.thread

/**
 * Module representative of this library. It performs and expose an interface
 * through which the app can perform some utility and handy tasks that are not tied
 * to any of the views exposed in this library.
 */
class ReactVisionModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    override fun getName() = NAME

    // START Module methods

    /**
     * Set the name of the output and location directory for the recognition model
     */
    @ReactMethod
    fun setModelOutputDirectoryName(name: String) = setModelDirName(name)

    /**
     * Get the location of the model output directory.
     */
    @ReactMethod
    fun getModelOutputDirectory(promise: Promise) = promise.resolve(getModelOutputDir())

    /**
     * Create the model output and captures output directories
     */
    @ReactMethod
    fun setupModelDirectories(promise: Promise) = prepareModelDirectories(context) {
        promise.resolve(true)
    }

    /**
     * Utility to trigger the recognition model
     */
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

    // END Module methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionModule"
    }
}