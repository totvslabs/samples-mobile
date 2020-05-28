package com.totvs.camera.events

class OnPictureTaken : Event {
    override fun invoke() {
    }

    companion object : Event.Export {
        override val name: String = "onPictureTaken"
    }
}