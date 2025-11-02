package io.rebble.pebblekit2.server

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.PebbleKitIntents
import io.rebble.pebblekit2.common.UniversalRequestResponse
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.fromBundle
import io.rebble.pebblekit2.common.model.toBundle
import io.rebble.pebblekit2.common.util.SuspendingBindingConnection
import io.rebble.pebblekit2.common.util.request
import java.util.UUID

/**
 * Connector that binds to the [io.rebble.pebblekit2.client.BasePebbleListenerService] and sends it data.
 *
 * [close] should be called after you are done using the class (when the app that started the listener is not active anymore).
 */
public class DefaultPebbleListenerConnector(
    context: Context,
    private val targetPackages: List<String>,
) : PebbleListenerConnector {
    private val connector = SuspendingBindingConnection<UniversalRequestResponse>(
        context,
        {
            val firstValidPackage = targetPackages.asSequence()
                .firstOrNull { packageName ->
                    val checkIntent = Intent(PebbleKitIntents.RECEIVE_DATA).setPackage(packageName)
                    context.packageManager.queryIntentServices(checkIntent, 0).isNotEmpty()
                } ?: return@SuspendingBindingConnection null

            Intent(PebbleKitIntents.RECEIVE_DATA).setPackage(firstValidPackage)
        },
        UniversalRequestResponse.Stub::asInterface
    )

    /**
     * A message has been received from the watch.
     *
     * @return null if the target app could not be reached
     */
    override suspend fun sendOnMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier,
    ): ReceiveResult? {
        val connection = connector.getOrConnect() ?: return null

        val bundle = bundleOf(
            PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_RECEIVE_DATA_FROM_WATCH,
            PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
            PebbleKitBundleKeys.KEY_DATA_DICTIONARY to data.toBundle(),
            PebbleKitBundleKeys.KEY_WATCH_ID to watch.value
        )

        val returnBundle = connection.request(bundle) ?: return null
        val resultBundle = returnBundle.getBundle(PebbleKitBundleKeys.KEY_RECEIVE_RESULT) ?: Bundle()

        return ReceiveResult.fromBundle(resultBundle)
    }

    /**
     * One of registered apps for this companion app has been opened on a watch
     *
     * @return true if message was delivered successfully
     */
    override suspend fun sendOnAppOpened(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ): Boolean {
        val connection = connector.getOrConnect() ?: return false

        val bundle = bundleOf(
            PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_APP_OPENED,
            PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
            PebbleKitBundleKeys.KEY_WATCH_ID to watch.value
        )

        connection.request(bundle)
        return true
    }

    /**
     * One of the previously-opened registered apps for this companion app has been closed on a watch. If this is the
     * last opened app, this service will self-terminate in several seconds.
     *
     * @return true if message was delivered successfully
     */
    override suspend fun sendOnAppClosed(
        watchappUUID: UUID,
        watch: WatchIdentifier,
    ): Boolean {
        val connection = connector.getOrConnect() ?: return false

        val bundle = bundleOf(
            PebbleKitBundleKeys.KEY_ACTION to PebbleKitBundleKeys.ACTION_APP_CLOSED,
            PebbleKitBundleKeys.KEY_WATCHAPP_UUID to watchappUUID.toString(),
            PebbleKitBundleKeys.KEY_WATCH_ID to watch.value
        )

        connection.request(bundle)
        return true
    }

    /**
     * Call when the target service should be destroyed (service is destroyed when all connectors to it are closed).
     *
     * We recommend you close a few seconds after its watchapps are closed to give the target app some time to clean up.
     */
    override fun close() {
        connector.close()
    }
}
