package com.totvs.clockin.vision.face

import androidx.annotation.Keep

/**
 * Clock-In specific purpose face object. This model class conforms specific
 * IPC format used for faces.
 */
@Keep
abstract class Face {
    /**
     * Name of the person recognized
     */
    abstract val name: String

    /**
     * Name of the person recognized
     */
    abstract val personId: String

    /**
     * Confidence of this face object. this might mean confidence of detection
     * or confidence of recognition.
     */
    abstract val distance: Float
}