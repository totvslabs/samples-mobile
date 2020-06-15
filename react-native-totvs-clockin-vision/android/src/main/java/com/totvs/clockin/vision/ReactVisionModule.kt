package com.totvs.clockin.vision

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

/**
 * Module representative of this library. It performs and expose an interface
 * through which the app can perform some utility and handy tasks that are not tied
 * to any of the views exposed in this library.
 */
class ReactVisionModule(
    private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    override fun getName() = NAME

    override fun getConstants(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    // START View methods
    // END View methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionModule"
    }
}