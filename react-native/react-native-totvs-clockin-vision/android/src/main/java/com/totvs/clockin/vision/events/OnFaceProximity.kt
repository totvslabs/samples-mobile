package com.totvs.clockin.vision.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

/**
 * Event emitted when the user face is too small to be eligible for
 * face recognition task.
 */
data class OnFaceProximity(
    val isUnderThreshold: Boolean = false,
    val threshold: Float,
    val faceWidth: Float,
    val faceHeight: Float
) : Event {

    override fun invoke(context: ReactContext, viewId: Int) {
        val event = Arguments.createMap().apply {
            putBoolean(FIELD_IS_UNDER_THRESHOLD, isUnderThreshold)
            putDouble(FIELD_THRESHOLD, threshold.toDouble())
            putDouble(FIELD_FACE_WIDTH, faceWidth.toDouble())
            putDouble(FIELD_FACE_HEIGHT, faceHeight.toDouble())
        }

        context.getJSModule(RCTEventEmitter::class.java).receiveEvent(viewId, name, event)
    }

    companion object : Event.Export {
        override val name = "onFaceProximity"

        private const val FIELD_IS_UNDER_THRESHOLD = "isUnderThreshold"
        private const val FIELD_THRESHOLD = "threshold"
        private const val FIELD_FACE_WIDTH = "faceWidth"
        private const val FIELD_FACE_HEIGHT = "faceHeight"
    }
}