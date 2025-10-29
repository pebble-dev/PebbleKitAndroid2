package io.rebble.pebblekit2.sample

import android.util.Log
import io.rebble.pebblekit2.client.BasePebbleListenerService
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import java.util.UUID

class PebbleListenerService : BasePebbleListenerService() {
    override suspend fun onMessageReceived(
        watchapUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult {
        Log.d("PebbleListenerService", "Received $data from app $watchapUUID on the watch $watch")
        return ReceiveResult.Ack
    }

    override fun onAppOpened(watchapUUID: UUID, watch: WatchIdentifier) {
        Log.d("PebbleListenerService", "App  $watchapUUID on the watch $watch opened")
    }

    override fun onAppClosed(watchapUUID: UUID, watch: WatchIdentifier) {
        Log.d("PebbleListenerService", "App  $watchapUUID on the watch $watch closed")
    }
}
