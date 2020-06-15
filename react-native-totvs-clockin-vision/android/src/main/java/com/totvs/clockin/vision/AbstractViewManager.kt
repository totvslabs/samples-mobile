package com.totvs.clockin.vision

import androidx.annotation.FloatRange
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.toCameraFacing

/**
 * Abstract class to abstract common operations between [CameraView] subclasses
 */
abstract class AbstractViewManager<T : CameraView> : ViewGroupManager<T>() {
    // START Setters methods
    @ReactProp(name = "facing")
    fun setFacing(cameraView: T, @LensFacing facing: Int) {
        cameraView.facing = facing.toCameraFacing
    }

    /**
     * Set initial camera zoom
     */
    @ReactProp(name = "zoom", defaultFloat = 0.0f)
    fun setZoom(
        cameraView: T,
        @FloatRange(from = 0.0, to = 1.0) zoom: Float
    ) {
        cameraView.zoom = zoom
    }
    // END Setters methods
}

