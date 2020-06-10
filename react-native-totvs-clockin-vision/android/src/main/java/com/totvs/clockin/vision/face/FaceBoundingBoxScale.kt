package com.totvs.clockin.vision.face

import android.graphics.RectF
import android.util.Size
import com.totvs.camera.view.GraphicOverlay
import com.totvs.camera.vision.face.FaceObject
import com.totvs.clockin.vision.graphic.StandardBoundsScaler

/**
 * Bound scaler for face bounds. This scaler is needed for receivers that need face objects
 * bounding box in the same coordinate system as [GraphicOverlay]
 */
class FaceBoundingBoxScale(overlay: GraphicOverlay) : StandardBoundsScaler<FaceObject>(overlay) {
    override fun FaceObject.clone(sourceSize: Size, boundingBox: RectF?): FaceObject =
        copy(sourceSize = sourceSize, boundingBox = boundingBox)
}