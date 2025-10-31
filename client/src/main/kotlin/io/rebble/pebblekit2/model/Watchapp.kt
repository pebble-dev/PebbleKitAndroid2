package io.rebble.pebblekit2.model

import java.util.UUID

public data class Watchapp(
    val id: UUID,
    val name: String?,
    val isWatchface: Type,
) {
    public enum class Type {
        WATCHFACE,
        WATCHAPP,
        UNKNOWN,
    }
}
