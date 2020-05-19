package com.totvs.camera

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

/**
 * @author Jansel Valentin
 */
class TOTVSCameraModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    override fun getName(): String = NAME

    /**
     * Constants exported to JS side
     */
    override fun getConstants(): MutableMap<String, Any> = mutableMapOf()

    companion object {
        /**
         * Exported name of the module representing this library
         */
        private const val NAME = "TOTVSCamera"
    }
}