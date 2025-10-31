package io.rebble.pebblekit2.client

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.os.bundleOf
import co.touchlab.kermit.Logger
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
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
            val selectedApp = PebbleAndroidAppPicker.getCurrentlySelectedApp(this@BasePebbleListenerService)

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
