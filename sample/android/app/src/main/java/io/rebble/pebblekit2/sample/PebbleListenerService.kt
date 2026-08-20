package io.rebble.pebblekit2.sample

import android.util.Log
import io.rebble.pebblekit2.client.BasePebbleListenerService
import io.rebble.pebblekit2.common.model.DataLogSession
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import java.util.UUID

class PebbleListenerService : BasePebbleListenerService() {
    override suspend fun onMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult {
        Log.d("PebbleListenerService", "Received $data from app $watchappUUID on the watch $watch")
        return ReceiveResult.Ack
    }

    override suspend fun onDataLogReceived(
        watchappUUID: UUID,
        session: DataLogSession,
        data: ByteArray,
        itemsLeft: Long,
        watch: WatchIdentifier,
    ): ReceiveResult {
        Log.d(
            "PebbleListenerService",
            "Received ${data.size / session.itemSize} data log items of session $session from app $watchappUUID"
        )
        return ReceiveResult.Ack
    }

    override suspend fun onDataLogSessionFinished(
        watchappUUID: UUID,
        session: DataLogSession,
        watch: WatchIdentifier,
    ): ReceiveResult {
        Log.d("PebbleListenerService", "Data log session $session from app $watchappUUID finished")
        return ReceiveResult.Ack
    }

    override fun onAppOpened(watchappUUID: UUID, watch: WatchIdentifier) {
        Log.d("PebbleListenerService", "App  $watchappUUID on the watch $watch opened")
    }

    override fun onAppClosed(watchappUUID: UUID, watch: WatchIdentifier) {
        Log.d("PebbleListenerService", "App  $watchappUUID on the watch $watch closed")
    }
}
