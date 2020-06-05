package com.totvs.clockin.vision.events

class OnFaceDetected : Event {
    override fun invoke() {
    }

    companion object : Event.Export {
        override val name: String = "onFaceDetected"
    }
}