package com.totvs.camera

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

/**
 * React Native module for this library
 */
public class ReactCameraModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    public override fun getName(): String =
        NAME

    /**
     * Constants exported to JS side
     */
    public override fun getConstants(): MutableMap<String, Any> = mutableMapOf()

    companion object {
        /**
         * Exported name of the module representing this library
         */
        private const val NAME = "CameraModule"
    }
}