package io.rebble.pebblekit2.server

import io.rebble.pebblekit2.common.model.DataLogSession
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
     * Send a batch of items from a data logging [session] of one of the registered apps to the
     * target app.
     *
     * [data] must contain only whole items: `data.size` must be a multiple of `session.itemSize`.
     * [itemsLeft] is the number of items that stay on the watch after this batch. Send the batches
     * of one session in sequence: await the result of a call before you send the next batch of
     * that session. Keep one call well below the 1 MB Android binder transaction limit; do not put
     * more than 100 KB of data into one call.
     *
     * [ReceiveResult.Ack] means that the target app stored the data. You can then discard it.
     * [ReceiveResult.Nack], [ReceiveResult.Unknown] (the target app has an old PebbleKit library)
     * and `null` mean that the target app did not store the data. Keep the data and send it again
     * later, a limited number of times. Discard the data when the attempts are used up or when the
     * target app is no longer installed. The target app must tolerate a batch that it gets more
     * than one time, so a retry after an unclear result is safe.
     *
     * @return null if the target app could not be reached
     */
    public suspend fun sendOnDataLogReceived(
        watchappUUID: UUID,
        session: DataLogSession,
        data: ByteArray,
        itemsLeft: Long,
        watch: WatchIdentifier,
    ): ReceiveResult? {
        return null
    }

    /**
     * Tell the target app that a data logging [session] of one of the registered apps is complete.
     * Send this only after the target app acknowledged all the batches of the session.
     *
     * The result has the same meaning as in [sendOnDataLogReceived]: on [ReceiveResult.Nack],
     * [ReceiveResult.Unknown] or `null`, send the event again later, a limited number of times.
     *
     * @return null if the target app could not be reached
     */
    public suspend fun sendOnDataLogSessionFinished(
        watchappUUID: UUID,
        session: DataLogSession,
        watch: WatchIdentifier,
    ): ReceiveResult? {
        return null
    }

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
