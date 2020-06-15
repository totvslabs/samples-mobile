package com.totvs.clockin.vision.view

import android.content.Context
import android.util.AttributeSet
import com.totvs.camera.view.CameraView
import com.totvs.clockin.vision.barcode.BarcodeVisionCamera

class BarcodeVisionCameraView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : CameraView(context, attrs, style), BarcodeVisionCamera