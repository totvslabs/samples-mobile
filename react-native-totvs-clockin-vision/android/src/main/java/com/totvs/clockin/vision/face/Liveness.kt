package com.totvs.clockin.vision.face

import android.content.Context
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.stream.VisionReceiver

/**
 * Liveness detection mode.
 */
sealed class Liveness : VisionReceiver<FaceObject> {
    abstract val id: Int

    override fun send(value: FaceObject) {
    }
}

/**
 * Detect face movement
 */
class LivenessFace(val context: Context, val viewId: Int) : Liveness() {
    override val id: Int = 1

    override fun toString() = TAG

    companion object {
        private const val TAG = "Liveness(Face)"
    }
}

/**
 * Detect eyes blinking
 */
class LivenessEyes(
    val context: Context,
    val viewId: Int,
    var requiredBlinks: Int = 0
) : Liveness() {
    override val id: Int = 2

    override fun toString() = TAG

    companion object {
        private const val TAG = "Liveness(Eyes)"
    }
}