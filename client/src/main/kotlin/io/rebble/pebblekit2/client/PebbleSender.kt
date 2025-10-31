package io.rebble.pebblekit2.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.PebbleKitIntents
import io.rebble.pebblekit2.common.UniversalRequestResponse
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.toBundle
import io.rebble.pebblekit2.common.util.SuspendingBindingConnection
import io.rebble.pebblekit2.common.util.request
import java.lang.AutoCloseable
import java.util.UUID

/**
 * Main class that sends information to the Pebble watch.
 *
 * [close] should be called after you are done using the class.
 */
public class PebbleSender(context: Context) : AutoCloseable {
    private val connector = SuspendingBindingConnection<UniversalRequestResponse>(
        context,
        {
            val targetPkg = PebbleAndroidAppPicker.getCurrentlySelectedApp(context)
                ?: return@SuspendingBindingConnection null
            Intent(PebbleKitIntents.SEND_DATA).setPackage(targetPkg)
        },
        UniversalRequestResponse.Stub::asInterface
    )

    /**
     * @param watchappUUID UUID of the app you are sending the data to
     * @param data data to send
     * @param watches list of watches to send data to. If *null*, the message will be sent to all connected watches
     * @return a map of transmission results for all requested watches
     * (or all connected watches if [watches] param was null).
     * This returns empty map if the [watches] param was null and there were no connected watches.
     * Return value is null if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    public suspend fun sendDataToPebble(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>? {
        val connection = connector.getOrConnect() ?: return null

        val bundle = bundleOf(
            PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_SEND_DATA_TO_WATCH,
            PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
            PebbleKitBundleKeys.KEY_DATA_DICTIONARY to data.toBundle(),
            PebbleKitBundleKeys.KEY_WATCHES_ID to watches?.map { it.value }?.toTypedArray()
        )

        val returnBundle = connection.request(bundle) ?: return null
        val resultsBundle = returnBundle.getBundle(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS) ?: Bundle()

        return resultsBundle.keySet().associate { key ->
            val transmissionResult = resultsBundle.getBundle(key)
                ?.let { TransmissionResult.fromBundle(it) }
                ?: TransmissionResult.Unknown("Missing TransmissionResult in PebbleSender result bundle")

            WatchIdentifier(key) to transmissionResult
        }
    }

    override fun close() {
        connector.close()
    }
}
