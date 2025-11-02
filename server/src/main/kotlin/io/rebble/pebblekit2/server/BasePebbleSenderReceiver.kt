package io.rebble.pebblekit2.server

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.os.bundleOf
import co.touchlab.kermit.Logger
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.mapFromBundle
import io.rebble.pebblekit2.common.model.toBundle
import io.rebble.pebblekit2.common.util.UniversalRequestResponseSuspending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.UUID

/**
 * A receiver for the [io.rebble.pebblekit2.client.PebbleSender] client class.
 *
 * All suspend methods in this class are called using [coroutineScope]. You can override it to provide your own scope.
 */
public abstract class BasePebbleSenderReceiver : Service() {
    protected open val coroutineScope: CoroutineScope = MainScope()

    public abstract suspend fun sendDataToPebble(
        callingPackage: String?,
        watchappUUID: UUID,
        data: PebbleDictionary,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>

    public abstract suspend fun startAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>

    public abstract suspend fun stopAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>

    override fun onBind(intent: Intent?): IBinder? {
        return Binder().asBinder()
    }

    private inner class Binder : UniversalRequestResponseSuspending(this, coroutineScope) {
        override suspend fun request(data: Bundle, callingPackage: String?): Bundle {
            val action = data.getString(PebbleKitBundleKeys.KEY_ACTION)
            return when (action) {
                PebbleKitBundleKeys.ACTION_SEND_DATA_TO_WATCH -> {
                    requestSendData(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_START_APP -> {
                    requestStartApp(data, callingPackage)
                }

                PebbleKitBundleKeys.ACTION_STOP_APP -> {
                    requestStopApp(data, callingPackage)
                }

                else -> {
                    LOGGER.w {
                        "Got unknown action ${action ?: "null"} from ${callingPackage ?: "null"}. " +
                            "Returning empty data..."
                    }
                    Bundle()
                }
            }
        }

        private suspend fun requestSendData(input: Bundle, callingPackage: String?): Bundle {
            val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)?.let { UUID.fromString(it) }
            if (watchappUuid == null) {
                LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "null"}. Returning empty data...." }
                return Bundle()
            }

            val watches = input.getStringArray(PebbleKitBundleKeys.KEY_WATCHES_ID)?.map { WatchIdentifier(it) }
            val dataBundle = input.getBundle(PebbleKitBundleKeys.KEY_DATA_DICTIONARY) ?: Bundle()
            val data = PebbleDictionaryItem.mapFromBundle(dataBundle)

            val results = sendDataToPebble(callingPackage, watchappUuid, data, watches)

            val transmissionResults = Bundle().apply {
                for ((key, value) in results) {
                    putBundle(key.value, value.toBundle())
                }
            }

            return bundleOf(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS to transmissionResults)
        }

        private suspend fun requestStartApp(input: Bundle, callingPackage: String?): Bundle {
            val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)?.let { UUID.fromString(it) }
            if (watchappUuid == null) {
                LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "null"}. Returning empty data...." }
                return Bundle()
            }

            val watches = input.getStringArray(PebbleKitBundleKeys.KEY_WATCHES_ID)?.map { WatchIdentifier(it) }

            val results = startAppOnTheWatch(watchappUuid, watches)

            val transmissionResults = Bundle().apply {
                for ((key, value) in results) {
                    putBundle(key.value, value.toBundle())
                }
            }

            return bundleOf(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS to transmissionResults)
        }

        private suspend fun requestStopApp(input: Bundle, callingPackage: String?): Bundle {
            val watchappUuid = input.getString(PebbleKitBundleKeys.KEY_WATCHAPP_UUID)?.let { UUID.fromString(it) }
            if (watchappUuid == null) {
                LOGGER.w { "Got a missing watchapp UUID from ${callingPackage ?: "null"}. Returning empty data...." }
                return Bundle()
            }

            val watches = input.getStringArray(PebbleKitBundleKeys.KEY_WATCHES_ID)?.map { WatchIdentifier(it) }

            val results = stopAppOnTheWatch(watchappUuid, watches)

            val transmissionResults = Bundle().apply {
                for ((key, value) in results) {
                    putBundle(key.value, value.toBundle())
                }
            }

            return bundleOf(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS to transmissionResults)
        }
    }
}

private val LOGGER = Logger.withTag(BasePebbleSenderReceiver::class.java.simpleName)
