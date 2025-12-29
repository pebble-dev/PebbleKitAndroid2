package io.rebble.pebblekit2.client.java

import android.content.Context
import io.rebble.pebblekit2.client.DefaultPebbleSender
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem

import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.function.Consumer

public class DefaultJavaPebbleSender @JvmOverloads constructor(
    private val context: Context,
    private val coroutineScope: CoroutineScope = MainScope(),
) : JavaPebbleSender {
    private val pebbleSender = DefaultPebbleSender(context)

    override fun sendDataToPebble(
        watchappUUID: UUID,
        data: Map<Int, PebbleDictionaryItem>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
        watches: List<WatchIdentifier>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.sendDataToPebble(watchappUUID, data.mapKeys { it.key.toUInt() }, watches)
            onResult.accept(result)
        }
    }

    override fun sendDataToPebble(
        watchappUUID: UUID,
        data: Map<Int, PebbleDictionaryItem>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.sendDataToPebble(watchappUUID, data.mapKeys { it.key.toUInt() })
            onResult.accept(result)
        }
    }

    override fun startAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>?,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.startAppOnTheWatch(watchappUUID, watches)
            onResult.accept(result)
        }
    }

    override fun startAppOnTheWatch(
        watchappUUID: UUID,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.startAppOnTheWatch(watchappUUID)
            onResult.accept(result)
        }
    }

    override fun stopAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.stopAppOnTheWatch(watchappUUID, watches)
            onResult.accept(result)
        }
    }

    override fun stopAppOnTheWatch(
        watchappUUID: UUID,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    ) {
        coroutineScope.launch {
            val result = pebbleSender.stopAppOnTheWatch(watchappUUID)
            onResult.accept(result)
        }
    }

    override fun close() {
        pebbleSender.close()
    }
}
