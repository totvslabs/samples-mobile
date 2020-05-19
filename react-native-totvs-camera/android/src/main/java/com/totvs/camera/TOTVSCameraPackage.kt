package com.totvs.camera

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * @author Jansel Valentin
 */
class TOTVSCameraPackage : ReactPackage {

    // Deprecated from RN 0.47
    override fun createJSModules(): MutableList<Class<out JavaScriptModule>> = mutableListOf()

    /**
     * Register modules for this library
     */
    override fun createNativeModules(
        context: ReactApplicationContext?
    ): MutableList<NativeModule> = mutableListOf(
        TOTVSCameraModule(context!!)
    )

    /**
     * Register managers for this library
     */
    override fun createViewManagers(
        context: ReactApplicationContext?
    ): MutableList<ViewManager<*, *>> = mutableListOf(
        TOTVSCameraManager()
    )
}