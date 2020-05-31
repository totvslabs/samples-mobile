package com.totvs.camera.view.events

class OnPictureTaken : Event {
    override fun invoke() {
    }

    companion object : Event.Export {
        override val name: String = "onPictureTaken"
    }
}