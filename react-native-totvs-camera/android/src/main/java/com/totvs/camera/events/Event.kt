package com.totvs.camera.events

/**
 * React Native JS event representation.
 *
 * Events emitted to the JS side are advised to implement this interface
 *
 * @author Jansel Valentin
 */
interface Event : () -> Unit {

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
        val exported = listOf<Export>(OnPictureTaken)

        /**
         * Fancy operator to enable for-each on Event.
         *
         * @see also [ReactCameraManager.getExportedCustomDirectEventTypeConstants]
         */
        operator fun iterator(): Iterator<Export> = exported.iterator()

        public fun forEach(block: (Export) -> Unit) = iterator().forEach(block)
    }
}