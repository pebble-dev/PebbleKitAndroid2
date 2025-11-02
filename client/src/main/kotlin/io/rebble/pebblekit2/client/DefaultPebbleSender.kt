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
import io.rebble.pebblekit2.common.model.fromBundle
import io.rebble.pebblekit2.common.model.toBundle
import io.rebble.pebblekit2.common.util.SuspendingBindingConnection
import io.rebble.pebblekit2.common.util.request
import java.util.UUID

/**
 * Main class that sends information to the Pebble watch.
 *
 * [close] should be called after you are done using the class.
 */
public class DefaultPebbleSender(context: Context) : PebbleSender {
    private val connector = SuspendingBindingConnection<UniversalRequestResponse>(
        context,
        {
            val targetPkg = DefaultPebbleAndroidAppPicker.getInstance(context).getCurrentlySelectedApp()
                ?: return@SuspendingBindingConnection null
            Intent(PebbleKitIntents.SEND_DATA).setPackage(targetPkg)
        },
        UniversalRequestResponse.Stub::asInterface
    )

    /**
     * Send an AppMessage to the app on the watch.
     *
     * Note that this requires your app's package to be listed in the app package.json's `companionApp` list.
     *
     * @param watchappUUID UUID of the app you are sending the data to
     * @param data data to send
     * @param watches list of watches to send data to. If *null*, the message will be sent to all connected watches
     * @return a map of transmission results for all requested watches
     * (or all connected watches if [watches] param was null).
     * This returns empty map if the [watches] param was null and there were no connected watches.
     *
     * Return value is null if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    override suspend fun sendDataToPebble(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watches: List<WatchIdentifier>?,
    ): Map<WatchIdentifier, TransmissionResult>? {
        return sendRequestForWatches(
            bundleOf(
                PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_SEND_DATA_TO_WATCH,
                PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
                PebbleKitBundleKeys.KEY_DATA_DICTIONARY to data.toBundle(),
                PebbleKitBundleKeys.KEY_WATCHES_ID to watches?.map { it.value }?.toTypedArray<String>()
            )
        )
    }

    /**
     * Start the app on the watch.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to start
     * @param watches list of watches to start the app on to. If *null*, the message will be sent to all connected watches
     * @return a map of transmission results for all requested watches
     * (or all connected watches if [watches] param was null).
     * This returns empty map if the [watches] param was null and there were no connected watches.
     *
     * Return value is null if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    override suspend fun startAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>?,
    ): Map<WatchIdentifier, TransmissionResult>? {
        return sendRequestForWatches(
            bundleOf(
                PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_START_APP,
                PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
                PebbleKitBundleKeys.KEY_WATCHES_ID to watches?.map { it.value }?.toTypedArray<String>()
            )
        )
    }

    /**
     * Stop the app on the watch.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to stop
     * @param watches list of watches to start the app on to. If *null*, the message will be sent to all connected watches
     * @return a map of transmission results for all requested watches
     * (or all connected watches if [watches] param was null).
     * This returns empty map if the [watches] param was null and there were no connected watches.
     *
     * Return value is null if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    override suspend fun stopAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>?,
    ): Map<WatchIdentifier, TransmissionResult>? {
        return sendRequestForWatches(
            bundleOf(
                PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_STOP_APP,
                PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
                PebbleKitBundleKeys.KEY_WATCHES_ID to watches?.map { it.value }?.toTypedArray<String>()
            )
        )
    }

    override fun close() {
        connector.close()
    }

    private suspend fun sendRequestForWatches(bundle: Bundle): Map<WatchIdentifier, TransmissionResult>? {
        val connection = connector.getOrConnect() ?: return null

        val returnBundle = connection.request(bundle) ?: return null
        val resultsBundle = returnBundle.getBundle(PebbleKitBundleKeys.KEY_TRANSMISSION_RESULTS) ?: Bundle()

        return resultsBundle.keySet().associate { key ->
            val transmissionResult = resultsBundle.getBundle(key)
                ?.let { TransmissionResult.fromBundle(it) }
                ?: TransmissionResult.Unknown("Missing TransmissionResult in PebbleSender result bundle")

            WatchIdentifier(key) to transmissionResult
        }
    }
}
