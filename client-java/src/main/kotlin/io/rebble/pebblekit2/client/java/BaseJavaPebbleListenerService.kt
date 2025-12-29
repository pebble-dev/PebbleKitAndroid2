package io.rebble.pebblekit2.client.java

import io.rebble.pebblekit2.client.BasePebbleListenerService
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID
import java.util.function.Consumer

/**
 * Main listener base class that receives data from the PebbleOS watches.
 *
 * Unlike normal base listener service, this one relies on callbacks instead of coroutines,
 * allowing it to be used in non-Kotlin projects.
 */
public abstract class BaseJavaPebbleListenerService : BasePebbleListenerService() {
    override suspend fun onMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult {
        val completableDeferred = CompletableDeferred<ReceiveResult>()

        onMessageReceived(
            watchappUUID,
            data.mapKeys { it.key.toInt() },
            watch.value,
            { completableDeferred.complete(it) },
        )

        return completableDeferred.await()
    }

    final override fun onAppOpened(watchappUUID: UUID, watch: WatchIdentifier) {
        onAppOpened(watchappUUID, watch.value)
    }

    final override fun onAppClosed(watchappUUID: UUID, watch: WatchIdentifier) {
        onAppClosed(watchappUUID, watch.value)
    }

    /**
     * A message has been received from the watch.
     *
     * Note that, on the receiving end, all received numbers in the dictionary are either
     * [PebbleDictionaryItem.UInt32] or [PebbleDictionaryItem.Int32], regardless of their original size on the watch.
     *
     * Passed [watch] parameter corresponds to the [WatchIdentifier.value].
     *
     * You MUST call [responder] after you are done processing this callback.
     */
    protected open fun onMessageReceived(
        watchappUUID: UUID,
        data: Map<Int, PebbleDictionaryItem>,
        /**
         * ID of the watch that
         */
        watch: String,
        responder: Consumer<ReceiveResult>,
    ) {
        responder.accept(ReceiveResult.Nack)
    }

    /**
     * One of registered apps for this companion app has been opened on a watch
     *
     * Passed [watch] parameter corresponds to the [WatchIdentifier.value].
     */
    protected open fun onAppOpened(watchappUUID: UUID, watch: String) {
    }

    /**
     * One of the previously-opened registered apps for this companion app has been closed on a watch. If this is the
     * last opened app, this service will self-terminate in several seconds.
     *
     * Passed [watch] parameter corresponds to the [WatchIdentifier.value].
     */
    protected open fun onAppClosed(watchappUUID: UUID, watch: String) {
    }
}
