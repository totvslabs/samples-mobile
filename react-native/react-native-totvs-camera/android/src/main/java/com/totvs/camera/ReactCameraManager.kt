package com.totvs.camera

import androidx.annotation.FloatRange
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.totvs.camera.core.annotations.LensFacing
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.toCameraFacing

/**
 * [CameraView] react native manager
 */
class ReactCameraManager : ViewGroupManager<CameraView>() {

    /**
     * React Native view name for the view managed by this manager
     */
    override fun getName() = VIEW_NAME

    /**
     * Create an instance of the view managed by this manager
     */
    @Suppress("MissingPermission")
    override fun createViewInstance(context: ThemedReactContext): CameraView =
        CameraView(context)

    // START Setters methods

    /**
     * Set the camera opening facing
     */
    @ReactProp(name = "facing")
    fun setFacing(cameraView: CameraView, @LensFacing facing: Int) {
        cameraView.facing = facing.toCameraFacing
    }

    /**
     * Set initial camera zoom
     */
    @ReactProp(name = "zoom", defaultFloat = 0.0f)
    fun setZoom(cameraView: CameraView, @FloatRange(from = 0.0, to = 1.0) zoom: Float) {
        cameraView.zoom = zoom
    }

    // END Setters methods

    companion object {
        /**
         * Name exported to react native. this will work as the component name. by convention
         * we name the manager with the same name as the view it manages.
         */
        private const val VIEW_NAME = "CameraView"
    }
}