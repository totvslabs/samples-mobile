package com.totvs.clockin.vision.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

/**
 * Event emitted when liveness feature is enabled and the kind of liveness
 * is triggered.
 */
class OnLiveness(private val mode: Int) : Event {
    override fun invoke(context: ReactContext, viewId: Int) {
        val event = Arguments.createMap().apply {
            putInt(FIELD_EVENT_MODE, mode)
        }
        context.getJSModule(RCTEventEmitter::class.java).receiveEvent(viewId, name, event)
    }

    companion object : Event.Export {
        override val name = "onLiveness"

        private const val FIELD_EVENT_MODE = "mode"
    }
}