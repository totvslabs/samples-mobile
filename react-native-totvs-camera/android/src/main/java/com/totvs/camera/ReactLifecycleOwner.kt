package com.totvs.camera

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.facebook.react.bridge.LifecycleEventListener

/**
 * LifecycleOwner to bridge with react-native environment
 *
 * @author Jansel Valentin
 * @see also [CameraView.lifecycleOwner]
 */
internal object ReactLifecycleOwner : LifecycleOwner, LifecycleEventListener {

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle = registry

    override fun onHostResume() = registry.handleLifecycleEvent(Lifecycle.Event.ON_START)

    override fun onHostPause()  = registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

    override fun onHostDestroy() = registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
}