package io.rebble.pebblekit2.model

import io.rebble.pebblekit2.common.model.WatchIdentifier

public data class ConnectedWatch(
    val id: WatchIdentifier,
    val name: String,
    val platform: String,
    val revision: String,
    val firmwareVersionMajor: Int,
    val firmwareVersionMinor: Int,
    val firmwareVersionPatch: Int,
    val firmwareVersionTag: String?,
)
