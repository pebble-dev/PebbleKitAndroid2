package io.rebble.pebblekit2.common.model

/**
 * Unique identifier of a specific watch. Its contents are an implementation detail of a specific Pebble Mobile app.
 */
@JvmInline
public value class WatchIdentifier(public val value: String)
