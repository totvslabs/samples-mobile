package com.totvs.clockin.vision

import com.facebook.react.bridge.ReactApplicationContext
import com.totvs.camera.view.core.ExportableConstant
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.clockin.vision.view.BarcodeVisionCameraView
import com.totvs.camera.view.core.ExportableConstant as CameraExportableConstants
import com.totvs.camera.vision.core.ExportableConstant as VisionExportableConstants


/**
 * [BarcodeVisionCameraView] react module interface
 */
class ReactVisionBarcodeModule(
    context: ReactApplicationContext
) : AbstractReactVisionModule(context) {

    override fun getName() = NAME

    /**
     * Export constants related to face vision capability.
     *
     * Modifications on this method are required to filter out constants not related to the face
     * capability. Nothing will happens if not filter is made but is advised.
     */
    override fun getConstants(): MutableMap<String, Any> {
        val constants = mutableMapOf<String, Any>()
        // let's export camera-view module exportable constants
        CameraExportableConstants.forEach { set ->
            constants[set.name] = set.export()
        }
        // let's export vision constants
        VisionExportableConstants.forEach { set ->
            // we only expose barcode formats as constants of this module.
            if (set != VisionBarcodeFormat) {
                return@forEach
            }
            constants[set.name] = set.export()
        }
        // now let's export constants of this module
        ExportableConstant.forEach { set ->
            constants[set.name] = set.export()
        }
        return constants
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