package com.totvs.clockin.vision.events

import com.facebook.react.bridge.ReactContext

/**
 * React Native JS event representation.
 *
 * Events emitted to the JS side are advised to implement this interface
 *
 */
interface Event : (ReactContext, Int) -> Unit {

    /**
     * Encodes information about the exportability of this event
     */
    interface Export {
        val name: String
    }

    /**
     * List of all exported events. Events not on this list wouldn't be considered to be
     * available on JS side.
     *
     * For kotlin code that doesn't want to have a dedicated class to represent an event
     * can create an object in this same file and register the event in the exported list as:
     *
     * ```
     *  object AnyEvent : Event.Export {
     *      override val name: String = "anyEvent"
     *  }
     * ```
     *
     * and
     * ```
     * val exported = listOf<Export>(..., AnnyEvent)
     * ```
     *
     * For java code it only suffix to create a class that implement [Export] interface
     * and register the event here by creating an instance of the class.
     */
    companion object {
        private val exported = listOf(
            OnBarcodeDetected,
            OnFaceRecognized,
            OnFaceProximity,
            OnLiveness
        )

        /**
         * Fancy operator to enable for-each on Event.
         */
        operator fun iterator(): Iterator<Export> = exported.iterator()

        inline fun forEach(block: (Export) -> Unit) = iterator().forEach(block)
    }
}