package com.totvs.clockin.vision

import androidx.annotation.AnyThread
import androidx.annotation.FloatRange
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.core.CameraFacingConstants
import com.totvs.camera.view.core.CameraZoomLimits
import com.totvs.camera.view.toCameraFacing
import com.totvs.camera.view.toFacingConstant
import com.totvs.camera.vision.core.VisionBarcodeFormat
import com.totvs.clockin.vision.barcode.VisionBarcodeCamera
import com.totvs.clockin.vision.view.VisionBarcodeCameraView
import com.totvs.camera.view.core.ExportableConstant as CameraExportableConstants
import com.totvs.camera.vision.core.ExportableConstant as VisionExportableConstants


/**
 * [VisionBarcodeCameraView] react module interface
 */
class VisionBarcodeModule(
    context: ReactApplicationContext
) : AbstractVisionModule(context) {

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
        return constants
    }

    /**
     * Convenience method to get a camera instance on a reified parameter.
     */
    private inline fun <R> Promise.withCamera(
        viewTag: Int,
        autoResolve: Boolean = true,
        crossinline block: VisionBarcodeCamera.(promise: Promise) -> R
    ) = withCameraDevice(viewTag, autoResolve, block)

    // START Module methods
    //
    // Note: react-native doesn't play well with inherited @ReactMethod. It doesn't find
    // inherited methods. That's the reason for logic duplication between Face/Barcode
    // Modules.

    /**
     * Set camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @AnyThread
    @ReactMethod
    fun setZoom(
        @FloatRange(from = 0.0, to = 1.0) zoom: Float,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.zoom = zoom
        true
    }

    /**
     * Get camera zoom. values ranges from 0 to 1 indicating the percentage of the zoom.
     *
     * Zoom value if expected to be one in between of the exported [CameraZoomLimits]
     */
    @AnyThread
    @ReactMethod
    fun getZoom(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { zoom }

    /**
     * Enable or disable camera torch
     */
    @AnyThread
    @ReactMethod
    fun enableTorch(
        enable: Boolean,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        isTorchEnabled = enable
        true
    }

    /**
     * whether or not the camera torch is enabled
     */
    @AnyThread
    @ReactMethod
    fun isTorchEnabled(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { isTorchEnabled }

    /**
     * Toggle the camera. i.e if the current camera is the front one it will toggle to back
     * camera as vice versa.
     */
    @AnyThread
    @ReactMethod
    fun toggleCamera(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) {
            toggleCamera()
            true
        }

    /**
     * Change the camera lens display. This is related to [toggleCamera] in the sense
     * that this method indicate explicitly which lens to use for the camera.
     *
     * [facing] is expressed as one of the exported constants [CameraFacingConstants]
     */
    @AnyThread
    @ReactMethod
    fun setLensFacing(
        @LensFacing facing: Int,
        viewTag: Int,
        promise: Promise
    ) = promise.withCamera(viewTag) {
        this.facing = facing.toCameraFacing
        true
    }

    /**
     * Get current camera facing. Returned facing is expected to be one of the exported
     * facing constants [CameraFacingConstants].
     */
    @AnyThread
    @ReactMethod
    fun getLensFacing(viewTag: Int, promise: Promise) =
        promise.withCamera(viewTag) { facing.toFacingConstant }

    // END Module methods

    companion object {
        /**
         * Exported name of the module representing this library vision capability
         */
        private const val NAME = "VisionBarcodeModule"
    }
}