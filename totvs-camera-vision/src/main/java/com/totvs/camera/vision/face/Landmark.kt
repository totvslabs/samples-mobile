package com.totvs.camera.vision.face

import android.graphics.PointF

/**
 * Representation of face landmarks
 */
interface Landmark {
    /**
     * The name of this landmark
     */
    val name: Name<*>

    /**
     * Interface that the name of this landmark must implement
     */
    interface Name<T : Landmark>
}

/**
 * Convenience way to have the name property in a super class
 */
abstract class AbstractLandmark(override val name: Landmark.Name<*>) : Landmark

/**
 * Null representation of a landmark. This value is used when purely null values are not
 * supported. This value was specifically created for [FaceObject.get] but we there relaxed
 * the constraint in order to support generic typed return.
 */
object NullLandmark : AbstractLandmark(NullLandmark), Landmark.Name<NullLandmark>

/**
 * Left eye landmark
 */
data class LeftEye(
    val position: PointF
) : AbstractLandmark(LeftEye) {
    companion object : Landmark.Name<LeftEye>
}

/**
 * Right eye landmark
 */
data class RightEye(
    val position: PointF
) : AbstractLandmark(RightEye) {
    companion object : Landmark.Name<RightEye>
}

/**
 * Accessor to know when this [Landmark] is null
 */
val Landmark?.isNull get() = this == null || this == NullLandmark
