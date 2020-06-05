package com.totvs.clockin.vision.events

class OnBarcodeDetected : Event {
    override fun invoke() {
    }

    companion object : Event.Export {
        override val name: String = "onBarcodeDetected"
    }
}