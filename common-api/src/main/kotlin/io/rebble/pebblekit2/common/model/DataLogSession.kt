package io.rebble.pebblekit2.common.model

/**
 * A data logging session. The watch makes a session when a watchapp calls `data_logging_create()`.
 *
 * The watchapp UUID, the [tag] and the [timestamp] identify a session. All items in a session
 * have the same [itemSize].
 */
public data class DataLogSession(
    /**
     * The tag that the watchapp gave to `data_logging_create()`. The watchapp uses different tags
     * for different types of data.
     */
    val tag: Long,

    /**
     * The Unix time, in seconds, when the watch made the session. It separates two sessions with
     * the same [tag], unless the watchapp made both in the same second.
     */
    val timestamp: Long,

    /**
     * The size, in bytes, of one data item.
     */
    val itemSize: Int,
) {
    public companion object
}
