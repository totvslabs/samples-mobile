package com.totvs.clockin.vision.events

import com.facebook.react.bridge.ReactContext

class OnBarcodeDetected : Event {
    override fun invoke(context: ReactContext, viewId: Int) {

    }

    companion object : Event.Export {
        override val name: String = "onBarcodeDetected"
    }
}