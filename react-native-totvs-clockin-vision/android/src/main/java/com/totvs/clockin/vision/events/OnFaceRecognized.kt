package com.totvs.clockin.vision.events

class OnFaceRecognized : Event {
    override fun invoke() {
    }
    companion object : Event.Export {
        override val name: String = "onFaceRecognized"
    }
}