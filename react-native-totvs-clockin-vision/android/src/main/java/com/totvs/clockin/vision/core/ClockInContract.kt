package com.totvs.clockin.vision.core

interface ClockInContract {

    var isBarcodeDetectionEnabled: Boolean

    var isFaceDetectionEnabled: Boolean

    fun takePicture()
}