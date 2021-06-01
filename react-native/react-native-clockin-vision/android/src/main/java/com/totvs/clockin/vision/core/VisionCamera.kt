package com.totvs.clockin.vision.core

import com.totvs.camera.core.Camera
import com.totvs.clockin.vision.face.VisionFaceCamera
import com.totvs.clockin.vision.barcode.VisionBarcodeCamera

/**
 * Camera with vision capabilities
 *
 * @see [VisionFaceCamera] and [VisionBarcodeCamera]
 */
interface VisionCamera : Camera