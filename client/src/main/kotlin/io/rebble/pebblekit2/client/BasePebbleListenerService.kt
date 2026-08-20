package io.rebble.pebblekit2.client

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.os.bundleOf
import co.touchlab.kermit.Logger
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.model.DataLogSession
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.fromBundle
import io.rebble.pebblekit2.common.model.mapFromBundle
import io.rebble.pebblekit2.common.model.toBundle
import io.rebble.pebblekit2.common.util.UniversalRequestResponseSuspending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.UUID

/**
 * Main listener base class that receives data from the PebbleOS watches.
 *
 * All suspend methods in this class are called using [coroutineScope]. You can override it to provide your own scope.
 */
public abstract class BasePebbleListenerService : Service() {
    protected open val coroutineScope: CoroutineScope = MainScope()

    /**
     * A message has been received from the watch.
     *
     * Note that, on the receiving end, all received numbers in the dictionary are either
     * [PebbleDictionaryItem.UInt32] or [PebbleDictionaryItem.Int32], regardless of their original size on the watch.
     */
    public open suspend fun onMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult {
        return ReceiveResult.Nack
    }

    /**
     * The watch sent a batch of items from a data logging [session] of one of the registered apps.
     *
     * Data logging is the store-and-forward alternative to messages. The watchapp writes items to
     * the watch storage, also when the phone is out of range. The watch sends the items when it is
     * connected.
     *
     * [data] contains `data.size / session.itemSize` full items, in the sequence the watchapp
     * logged them. [itemsLeft] is the number of items that stay on the watch after this batch.
     *
     * Return [ReceiveResult.Ack] only after you stored the data. The Pebble app can then discard
     * it. Return [ReceiveResult.Nack] if you could not store the data. The Pebble app can then try
     * the delivery again later. The Pebble app can send the same batch more than one time; store
     * the items so that a repeated batch does not add duplicate data.
     */
    public open suspend fun onDataLogReceived(
        watchappUUID: UUID,
        session: DataLogSession,
        data: ByteArray,
        itemsLeft: Long,
        watch: WatchIdentifier,
    ): ReceiveResult {
        return ReceiveResult.Nack
    }

    /**
     * A data logging [session] of one of the registered apps is complete. The watchapp called
     * `data_logging_finish()`, and the Pebble app sends this event after you acknowledged all the
     * batches of the session.
     *
     * Return [ReceiveResult.Ack] after you processed the event. Return [ReceiveResult.Nack] if you
     * could not process it. The Pebble app can then send the event again later.
     */
    public open suspend fun onDataLogSessionFinished(
        watchappUUID: UUID,
        session: DataLogSession,
        watch: WatchIdentifier,
    ): ReceiveResult {
        return ReceiveResult.Nack
    }

    /**
     * One of registered apps for this companion app has been opened on a watch
     */
    public open fun onAppOpened(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ) {
    }

    /**
     * One of the previously-opened registered apps for this companion app has been closed on a watch. If this is the
     * last opened app, this service will self-terminate in several seconds.
     */
    public open fun onAppClosed(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ) {
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder().asBinder()
    }

    private inner class Binder : UniversalRequestResponseSuspending(this, coroutineScope) {
        override suspend fun request(data: Bundle, callingPackage: String?): Bundle {
            val selectedApp = DefaultPebbleAndroidAppPicker.getInstance(this@BasePebbleListenerService)
                .getCurrentlySelectedApp()

            if (selectedApp != callingPackage) {
                LOGGER.w {
                    "Got message from non-selected app: ${callingPackage ?: "UNKNOWN"}" +
                        ". Selected app: ${selectedApp ?: "NONE"}."
                }

                return Bundle()
            }

            val action = data.getString(PebbleKitBundleKeys.KEY_ACTION)
            return when (action) {
                PebbleKitBundleKeys.ACTION_RECEIVE_DATA_FROM_WATCH -> {
                    handleReceiveData(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_DATA_LOG_RECEIVED -> {
                    handleDataLogReceived(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_DATA_LOG_SESSION_FINISHED -> {
                    handleDataLogSessionFinished(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_APP_OPENED -> {
                    handleAppOpened(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_APP_CLOSED -> {
                    handleAppClosed(data, callingPackage)
                }

                else -> {
                    LOGGER.w {
                        "Got unknown action ${action ?: "UNKNOWN"} from ${callingPackage ?: "UNKNOWN"}. " +
                            "Ignoring event..."
                    }
                    Bundle()
                }
            }
        }
    }

    private suspend fun handleReceiveData(input: Bundle, callingPackage: String?): Bundle {
        val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)
            ?.let { UUID.fromString(it) }
        if (watchappUuid == null) {
            LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val watchId = input.getString(PebbleKitBundleKeys.KEY_WATCH_ID)
            ?.let { WatchIdentifier(it) }
        if (watchId == null) {
            LOGGER.w { "Got a missing watch ID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val dataBundle = input.getBundle(PebbleKitBundleKeys.KEY_DATA_DICTIONARY) ?: Bundle()
        val data = PebbleDictionaryItem.mapFromBundle(dataBundle)

        val result = onMessageReceived(watchappUuid, data, watchId)

        return bundleOf(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS to result.toBundle())
    }

    private suspend fun handleDataLogReceived(input: Bundle, callingPackage: String?): Bundle {
        val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)
            ?.let { UUID.fromString(it) }
        if (watchappUuid == null) {
            LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val watchId = input.getString(PebbleKitBundleKeys.KEY_WATCH_ID)
            ?.let { WatchIdentifier(it) }
        if (watchId == null) {
            LOGGER.w { "Got a missing watch ID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val data = input.getByteArray(PebbleKitBundleKeys.KEY_DATA_LOG_DATA)
        if (data == null) {
            LOGGER.w { "Got missing data log data from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val session = validDataLogSession(input, data, callingPackage) ?: return Bundle()

        if (!input.containsKey(PebbleKitBundleKeys.KEY_DATA_LOG_ITEMS_LEFT)) {
            LOGGER.w { "Got a missing data log items-left from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }
        val itemsLeft = input.getLong(PebbleKitBundleKeys.KEY_DATA_LOG_ITEMS_LEFT)

        val result = onDataLogReceived(watchappUuid, session, data, itemsLeft, watchId)

        return bundleOf(PebbleKitBundleKeys.KEY_RECEIVE_RESULT to result.toBundle())
    }

    private fun validDataLogSession(input: Bundle, data: ByteArray, callingPackage: String?): DataLogSession? {
        val sessionBundle = input.getBundle(PebbleKitBundleKeys.KEY_DATA_LOG_SESSION)
        if (sessionBundle == null) {
            LOGGER.w { "Got a missing data log session from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return null
        }

        val session = DataLogSession.fromBundle(sessionBundle)
        if (session.itemSize <= 0 || data.size % session.itemSize != 0) {
            LOGGER.w { "Got an invalid data log batch from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return null
        }

        return session
    }

    private suspend fun handleDataLogSessionFinished(input: Bundle, callingPackage: String?): Bundle {
        val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)
            ?.let { UUID.fromString(it) }
        if (watchappUuid == null) {
            LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val watchId = input.getString(PebbleKitBundleKeys.KEY_WATCH_ID)
            ?.let { WatchIdentifier(it) }
        if (watchId == null) {
            LOGGER.w { "Got a missing watch ID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val sessionBundle = input.getBundle(PebbleKitBundleKeys.KEY_DATA_LOG_SESSION)
        if (sessionBundle == null) {
            LOGGER.w { "Got a missing data log session from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val result = onDataLogSessionFinished(watchappUuid, DataLogSession.fromBundle(sessionBundle), watchId)

        return bundleOf(PebbleKitBundleKeys.KEY_RECEIVE_RESULT to result.toBundle())
    }

    private fun handleAppOpened(input: Bundle, callingPackage: String?): Bundle {
        val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)
            ?.let { UUID.fromString(it) }
        if (watchappUuid == null) {
            LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val watchId = input.getString(PebbleKitBundleKeys.KEY_WATCH_ID)
            ?.let { WatchIdentifier(it) }
        if (watchId == null) {
            LOGGER.w { "Got a missing watch ID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        onAppOpened(watchappUuid, watchId)

        return Bundle()
    }

    private fun handleAppClosed(input: Bundle, callingPackage: String?): Bundle {
        val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)
            ?.let { UUID.fromString(it) }
        if (watchappUuid == null) {
            LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        val watchId = input.getString(PebbleKitBundleKeys.KEY_WATCH_ID)
            ?.let { WatchIdentifier(it) }
        if (watchId == null) {
            LOGGER.w { "Got a missing watch ID from ${callingPackage ?: "UNKNOWN"}. Ignoring event..." }
            return Bundle()
        }

        onAppClosed(watchappUuid, watchId)

        return Bundle()
    }
}

private val LOGGER = Logger.withTag(BasePebbleListenerService::class.java.simpleName)
