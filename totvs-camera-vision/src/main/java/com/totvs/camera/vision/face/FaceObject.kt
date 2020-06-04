package com.totvs.camera.vision.face

import android.graphics.RectF
import android.util.Size
import com.totvs.camera.vision.VisionObject

/**
 * Vision object that represents a detected barcode.
 *
 * Encoded information on this object, depends on the kind of detector used.
 *
 * While designing this class we took the decision to leave [landmarks] as a data class
 * param even though is not meant to be accessible to the exterior, because we want automatic
 * consideration of it on copy, equals and hashCode generated code. This can lead to some
 * interesting consequences, like the following:
 * 1. The field is private to the caller but modifiable by the class itself
 * 2. The caller have the power to freeze the list of landmarks. In this case, any attempt
 * to modify the landmarks won't have any effect.
 */
data class FaceObject(
    override val sourceSize: Size = Size(0, 0),
    override val boundingBox: RectF? = null,
    override val sourceRotationDegrees: Int = -1,
    private val landmarks: List<Landmark> = mutableListOf()
) : VisionObject() {

    /**
     * Retrieve any landmark on this face if it is present. If this face doesn't
     * have the requested [Landmark], then null is returned, the actual
     * landmark otherwise.
     *
     * If the receiver of this operation is [NullFaceObject] then
     * null will be returned for any [name]
     */
    operator fun <T : Landmark> get(name: Landmark.Name<T>): T? {
        return if (this == NullFaceObject) {
            null
        } else {
            val landmark = landmarks.firstOrNull {
                it.name == name
            }
            // if somehow [NullLandmark] ended up here, we just return null
            @Suppress("unchecked_cast")
            if (landmark.isNull) null else landmark as? T
        }
    }

    /**
     * Set any landmark on this [FaceObject]. [landmark] suffix to register
     * the landmark into this face but is a language requirement that this
     * operator receives at least two operands.
     *
     * If the receiver of this operation is [NullFaceObject] then nothing
     * will happens and the operation won't modify the object. i.e won't register
     * the landmark.
     *
     * [NullLandmark] can't be registered into a face.
     */
    operator fun set(name: Landmark.Name<*>, landmark: Landmark) {
        if (this == NullFaceObject || name == NullLandmark) {
            return
        }
        if (landmarks is MutableList<Landmark>) {
            // drop all the landmarks with same name
            landmarks.removeAll { it.name == name }
            // add the fresh one
            landmarks.add(landmark)
        }
    }

    /**
     * Iterate over landmarks.
     */
    operator fun iterator(): Iterator<Landmark> = landmarks.iterator()

    /**
     * ForEach construction on face landmarks
     */
    fun forEach(block: (Landmark) -> Unit) = iterator().forEach(block)
}

/**
 * Null representation of a null face object.
 */
val NullFaceObject = FaceObject(sourceSize = Size(Int.MIN_VALUE, Int.MIN_VALUE))

/**
 * Accessor to know when this [VisionObject] is null
 */
val FaceObject.isNull get() = this == NullFaceObject