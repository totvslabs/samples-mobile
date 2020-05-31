package com.totvs.camera.vision.stream

import androidx.annotation.GuardedBy
import com.totvs.camera.vision.VisionObject

/**
 * [VisionStream] that keep a reference of all the receivers that have connected
 * to it and dispose then appropriately.
 */
internal class BroadcastVisionStream : VisionStream<VisionObject> {

    @GuardedBy("this")
    private val receivers = mutableListOf<VisionReceiver<VisionObject>>()

    /**
     * Broadcast to receivers the incoming [VisionObject]
     */
    @Synchronized
    fun broadcast(entity: VisionObject) {
        receivers.forEach { it.send(entity) }
    }

    /**
     * Add a [receiver] to this stream
     */
    @Synchronized
    fun add(receiver: VisionReceiver<VisionObject>) {
        receivers.remove(receiver)
        receivers.add(receiver)
    }

    /**
     * Remove [receiver] from this stream
     */
    @Synchronized
    fun remove(receiver: VisionReceiver<VisionObject>) {
        receivers.remove(receiver)
    }

    override fun connect(receiver: VisionReceiver<VisionObject>): Connection {
        return ReceiverConnection(receiver.also {
            add(receiver)
        })
    }

    /**
     * Internal class that automatically remove the receiver from
     * the registry of receiver at [disconnect] moment.
     */
    inner class ReceiverConnection(
        private val receiver: VisionReceiver<VisionObject>
    ) : Connection {
        override fun disconnect() = remove(receiver)
    }
}