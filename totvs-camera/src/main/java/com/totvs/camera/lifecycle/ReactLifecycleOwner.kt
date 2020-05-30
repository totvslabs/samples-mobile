package com.totvs.camera.lifecycle

import androidx.annotation.RestrictTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.facebook.react.bridge.LifecycleEventListener
import com.totvs.camera.view.CameraView

/**
 * LifecycleOwner to bridge with react-native environment
 *
 * @see also [CameraView.lifecycle]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object ReactLifecycleOwner : LifecycleOwner, LifecycleEventListener {

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle =
        registry

    override fun onHostResume() = registry.handleLifecycleEvent(Lifecycle.Event.ON_START)

    override fun onHostPause()  = registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

    override fun onHostDestroy() = registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
}