package com.totvs.clockin.vision

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.clockin.vision.core.ExportableConstant
import com.totvs.clockin.vision.events.OnFaceRecognized
import com.totvs.clockin.vision.face.VisionFaceCamera
import com.totvs.clockin.vision.face.VisionFaceCamera.RecognitionOptions
import com.totvs.clockin.vision.utils.getModelOutputDir
import com.totvs.clockin.vision.view.VisionFaceCameraView
import java.io.File
import com.totvs.camera.view.core.ExportableConstant as CameraExportableConstants
import com.totvs.camera.vision.core.ExportableConstant as VisionExportableConstants

/**
 * [VisionFaceCameraView] react module interface
 */
class ReactVisionFaceModule(
    private val context: ReactApplicationContext
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
            // we don't expose barcode formats as constants of this module.
            if (set == VisionBarcodeFormat) {
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

    /**
     * Convenience method to get a camera instance on a reified parameter.
     */
    private inline fun <R> Promise.withCamera(
        viewTag: Int,
        autoResolve: Boolean = true,
        crossinline block: VisionFaceCamera.(promise: Promise) -> R
    ) = withCameraDevice(viewTag, autoResolve, block)

    // START Module methods

    /**
     * Trigger the recognition on an still picture. If [saveImage] is true, then the result will
     * contain a path for the saved image.
     * Results of this method are obtained through the dispatch of the [OnFaceRecognized] event
     */
    @ReactMethod
    fun recognizeStillPicture(viewTag: Int, saveImage: Boolean, promise: Promise) =
        promise.withCamera(viewTag) {
            recognizeStillPicture(
                RecognitionOptions(
                    saveImage = saveImage,
                    outputDir = File(getModelOutputDir())
                )
            ) { result ->
                // let's dispatch the [OnFaceRecognized] event
                OnFaceRecognized(result.file, result.faces)(context, viewTag)
            }
        }

    // END Module methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionFaceModule"
    }
}