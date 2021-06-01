package com.totvs.clockin.vision

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * React Native package for this library.
 */
class VisionPackage : ReactPackage {
    /**
     * Register modules for this library
     */
    override fun createNativeModules(
        context: ReactApplicationContext
    ): List<NativeModule> = listOf(
        VisionModule(context),
        VisionFaceModule(context),
        VisionBarcodeModule(context)
    )

    /**
     * Register managers for this library.
     */
    override fun createViewManagers(
        context: ReactApplicationContext
    ): List<ViewManager<*, *>> = listOf(
        VisionFaceManager(),
        VisionBarcodeManager()
    )
}