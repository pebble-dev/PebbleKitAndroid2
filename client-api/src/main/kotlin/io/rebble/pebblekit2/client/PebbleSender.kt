package io.rebble.pebblekit2.client

import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import java.lang.AutoCloseable
import java.util.UUID

public interface PebbleSender : AutoCloseable {
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
    public suspend fun sendDataToPebble(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>?

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
    public suspend fun startAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>?

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
    public suspend fun stopAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>? = null,
    ): Map<WatchIdentifier, TransmissionResult>?
}
