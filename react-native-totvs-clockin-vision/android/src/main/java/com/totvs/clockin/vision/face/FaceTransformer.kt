package com.totvs.clockin.vision.face

import android.graphics.PointF
import android.graphics.RectF
import android.util.Size
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.Landmark
import com.totvs.camera.vision.face.LeftEye
import com.totvs.camera.vision.face.Nose
import com.totvs.camera.vision.stream.VisionReceiver
import com.totvs.clockin.vision.graphic.StandardBoundsScaler

/**
 * Scaler to scale face bounds. This scaler is needed for receivers that need face objects
 * bounding box in the same coordinate system as [GraphicOverlay].
 */
class FaceBoundsScaler(overlay: GraphicOverlay) : StandardBoundsScaler<FaceObject>(overlay) {
    override fun FaceObject.clone(sourceSize: Size, boundingBox: RectF?): FaceObject =
        copy(sourceSize = sourceSize, boundingBox = boundingBox)
}

/**
 * Scaler to translate the nose landmark. This scaler is needed for receivers that need face objects
 * landmarks in the same coordinate system as [GraphicOverlay].
 *
 * This translator is only used for liveness face since we need to do some processing on
 * the nose coordinate.
 */
class FaceNoseTranslator(overlay: GraphicOverlay) : StandardBoundsScaler<FaceObject>(overlay) {
    override fun FaceObject.clone(sourceSize: Size, boundingBox: RectF?): FaceObject {
        // her instead of overriding transform to scale the landmark points
        // we take the advantage of the fact that [StandardBoundsScaler] computes
        // all the scales parameter before calling this method, hence we can use those
        // scales to fix landmark points.
        // We do two things at once: 1. copying the upstream object. 2. fix the landmarks points.
        return copy(sourceSize = sourceSize, boundingBox = boundingBox).apply {
            this[Nose]?.let {
                val x = translateX(it.position.x)
                val y = translateY(it.position.y)

                this[Nose] = it.copy(position = PointF(x, y))
            }
        }
    }
}