package com.totvs.clockin.vision.core

/**
 * Interface to express the result of models installed in this libvrary.
 */
interface ModelOutput<out T> {
    /**
     * List of entities recognized/detected by the model.
     */
    val entities: List<T>

    /**
     * Status of the operation, can be anything relevant to the consumer.
     */
    val status: String

    /**
     * Encoding information about the operation, can be anything relevant to the consumer
     */
    val encoding: String
}

@Suppress("FunctionName")
inline fun <T> ModelOutput(
    entities: List<T> = emptyList(),
    status: String = "<empty>",
    encoding: String = "<empty>"
): ModelOutput<T> = object : ModelOutput<T> {
    override val entities: List<T> get() = entities
    override val status: String get() = status
    override val encoding: String get() = encoding
}