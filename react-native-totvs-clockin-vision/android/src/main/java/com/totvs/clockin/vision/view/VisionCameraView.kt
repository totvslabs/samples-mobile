package com.totvs.clockin.vision.view

import com.totvs.camera.view.core.CameraViewModuleOptions
import com.totvs.camera.vision.core.VisionModuleOptions

class VisionCameraView {
    init { // debug purpose
        VisionModuleOptions.DEBUG_ENABLED = true
        CameraViewModuleOptions.DEBUG_ENABLED = true
    }
}