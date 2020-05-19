package com.totvs.camera.bridge

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * React Native package for this library.
 *
 * Please keep in mind that the runtime react-native library used to compile
 * the project is different from the version resolved by gradle, which is around
 * 0.20.1.
 *
 * It turns out that the 0.20.1 library is not compatible with the runtime found
 * in node_modules. That said, don't fix the compile time errors on this file. They're there
 * to reflect the actual signature of the runtime library with which the project is
 * compiled.
 *
 * @author Jansel Valentin
 */
class TOTVSCameraPackage : ReactPackage {
    // Deprecated and don't implement this method. Read above
//    override fun createJSModules(): List<Class<out JavaScriptModule>> = emptyList()

    /**
     * Register modules for this library
     */
    override fun createNativeModules(
        context: ReactApplicationContext
    ): List<NativeModule> = listOf(
        TOTVSCameraModule(context)
    )

    /**
     * Register managers for this library.
     */
    override fun createViewManagers(
        context: ReactApplicationContext
    ): List<ViewManager<*, *>> = listOf(
        TOTVSCameraManager()
    )
}