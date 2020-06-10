package com.totvs.clockin.vision.face

import android.content.Context
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Proximity Detector
 */
interface Proximity : VisionReceiver<FaceObject>

/**
 * Proximity Detector that act on [FaceObject] width dimensions.
 */
class ProximityByFaceWidth(val context: Context, val viewId: Int) : Proximity {
    /**
     * Value that limit the face width to determine when the face width is right, hence
     * regarded as close.
     */
    var threshold: Float = 100f
        set(value) = synchronized(this) {
            field = value
        }
        get() = synchronized(this) {
            field
        }

    override fun send(value: FaceObject) {
    }

    companion object {
        private const val TAG = "ProximityByFaceWidth"
    }
}