package com.totvs.clockin.vision.events

class OnPictureTaken : Event {
    override fun invoke() {
    }

    companion object : Event.Export {
        override val name: String = "onPictureTaken"
    }
}