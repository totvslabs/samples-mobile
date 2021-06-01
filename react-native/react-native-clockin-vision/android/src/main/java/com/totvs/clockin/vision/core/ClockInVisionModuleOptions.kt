package com.totvs.clockin.vision.core

object ClockInVisionModuleOptions {
    var DEBUG_ENABLED = false

    /**
     * Controls whether detection is ran on captured still picture before performing recognition.
     *
     * This is related to the inability of the cpp lib to perform well on images
     * with high light source.
     */
    var USES_STILL_CAPTURES_DETECTION = false
}