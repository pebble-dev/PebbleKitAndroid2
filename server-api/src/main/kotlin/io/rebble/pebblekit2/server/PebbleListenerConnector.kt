package io.rebble.pebblekit2.server

import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import java.lang.AutoCloseable
import java.util.UUID

public interface PebbleListenerConnector : AutoCloseable {
    /**
     * A message has been received from the watch.
     *
     * @return null if the target app could not be reached
     */
    public suspend fun sendOnMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult?

    /**
     * One of registered apps for this companion app has been opened on a watch
     *
     * @return true if message was delivered successfully
     */
    public suspend fun sendOnAppOpened(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ): Boolean

    /**
     * One of the previously-opened registered apps for this companion app has been closed on a watch. If this is the
     * last opened app, this service will self-terminate in several seconds.
     *
     * @return true if message was delivered successfully
     */
    public suspend fun sendOnAppClosed(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ): Boolean
}
