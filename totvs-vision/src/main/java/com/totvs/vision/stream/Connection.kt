package com.totvs.vision.stream

/**
 * [Connection] represent the connection of a receiver to an stream. It can be
 * used to disconnect from the upstream stream.
 *
 * It's recommended to disconnect from upstream after being done with it.
 */
interface Connection {
    /**
     * Trigger to the upstream a disconnect token
     */
    fun disconnect()
}