package com.totvs.clockin.vision

import com.facebook.react.bridge.ReactApplicationContext
import com.totvs.clockin.vision.view.BarcodeVisionCameraView

/**
 * [BarcodeVisionCameraView] react module interface
 */
class ReactVisionBarcodeModule(
    context: ReactApplicationContext
) : AbstractReactVisionModule(context) {

    override fun getName() = NAME

    override fun getConstants(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    // START View methods

    // @TODO(jansel) - not yet implemented

    // END View methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionBarcodeModule"
    }
}