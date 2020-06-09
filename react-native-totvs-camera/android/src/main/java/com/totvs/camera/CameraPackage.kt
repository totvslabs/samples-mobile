package com.totvs.camera

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * React Native package for this library.
 */
/*
 * Please keep in mind that the runtime react-native library used to compile
 * the library is different from the version resolved by gradle when used in a react-native
 * project. The version resolved by gradle which is around 0.20.1 while the one resolved
 * when compiling the library included in react native project depends on the version
 * used when creating the project.
 *
 * It turns out that the 0.20.1 library is not compatible with the runtime found
 * in node_modules. That said, DON'T fix the compile time errors on this file. They're there
 * to reflect the actual signature of the runtime library with which the project is
 * compiled.
 *
 * When you work on this library you can specify on build.gradle the node_modules directory
 * to be used as a source for the react-native library implementation to allow this code
 * to compile.
 *
 * Read documentation in react-native-totvs-camera/build.gradle to know how to handle this
 * situation for compile time.
 *
 * @see also [ReactCameraModule]
 */
public class CameraPackage : ReactPackage {
    /**
     * Register modules for this library
     */
    override fun createNativeModules(
        context: ReactApplicationContext
    ): List<NativeModule> = listOf(
        CameraModule(context)
    )

    /**
     * Register managers for this library.
     */
    override fun createViewManagers(
        context: ReactApplicationContext
    ): List<ViewManager<*, *>> = listOf(
        CameraManager()
    )
}