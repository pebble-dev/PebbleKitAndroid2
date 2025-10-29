package io.rebble.pebblekit2.server

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.os.bundleOf
import co.touchlab.kermit.Logger
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.SendDataCallback
import io.rebble.pebblekit2.common.UniversalRequestResponse
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.toBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
        app: UUID,
        data: PebbleDictionary,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>

    override fun onBind(intent: Intent?): IBinder? {
        return Binder().asBinder()
    }

    private inner class Binder : UniversalRequestResponse.Stub() {
        override fun request(data: Bundle, callback: SendDataCallback) {
            val callingPackage = packageManager.getNameForUid(getCallingUid())

            coroutineScope.launch {
                val action = data.getString(PebbleKitBundleKeys.KEY_ACTION)
                val result = if (action == PebbleKitBundleKeys.ACTION_SEND_DATA_TO_WATCH) {
                    requestSendData(data, callingPackage)
                } else {
                    LOGGER.w {
                        "Got unknown action ${action ?: "null"} from ${callingPackage ?: "null"}. " +
                            "Returning empty data..."
                    }
                    Bundle()
                }

                callback.onResult(result)
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
    }
}

private val LOGGER = Logger.withTag(BasePebbleSenderReceiver::class.java.simpleName)
