package com.totvs.clockin.vision

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.totvs.clockin.vision.view.BarcodeVisionCameraView

/**
 * [BarcodeVisionCameraView] react module interface
 */
class ReactVisionBarcodeModule(
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
        private const val NAME = "VisionBarcodeModule"
    }
}