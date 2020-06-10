package com.totvs.clockin.vision.core

import com.totvs.camera.core.Camera
import com.totvs.clockin.vision.face.FaceVisionCamera
import com.totvs.clockin.vision.barcode.BarcodeVisionCamera

/**
 * Camera with vision capabilities
 *
 * @see [FaceVisionCamera] and [BarcodeVisionCamera]
 */
interface VisionCamera : Camera