package io.rebble.pebblekit2.client.java

import io.rebble.pebblekit2.client.PebbleAndroidAppPicker
import io.rebble.pebblekit2.client.PebbleSender
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.TransmissionResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import java.util.UUID
import java.util.function.Consumer

/**
 * A variant of the [PebbleSender] that relies on callbacks instead of coroutines,
 * allowing it to be used in non-Kotlin projects.
 */
public interface JavaPebbleSender : AutoCloseable {
    /**
     * Send an AppMessage to the app on the watch.
     *
     * Note that this requires your app's package to be listed in the app package.json's `companionApp` list.
     *
     * @param watchappUUID UUID of the app you are sending the data to
     * @param data data to send
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all requested watches
     *   or null if a Pebble app is not reachable
     *   (for example if not installed or limited by the [PebbleAndroidAppPicker])
     * @param watches list of watches to send data to
     */
    public fun sendDataToPebble(
        watchappUUID: UUID,
        data: Map<Int, PebbleDictionaryItem>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
        watches: List<WatchIdentifier>,
    )

    /**
     * Send an AppMessage to the app on the watch.
     *
     * Note that this requires your app's package to be listed in the app package.json's `companionApp` list.
     *
     * @param watchappUUID UUID of the app you are sending the data to
     * @param data data to send
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all connected watches,
     *  an empty map there were no connected watches,
     *   or null if a Pebble app is not reachable
     *   (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    public fun sendDataToPebble(
        watchappUUID: UUID,
        data: Map<Int, PebbleDictionaryItem>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    )

    /**
     * Start the app on the specified watches.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to start
     * @param watches list of watches to start the app on to. If *null*, the message will be sent to all connected watches
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all requested watches
     *   or null if a Pebble app is not reachable
     *   (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    public fun startAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>? = null,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    )

    /**
     * Start the app on all connected watch.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to start
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all connected watches,
     *  an empty map there were no connected watches,
     *   or null if a Pebble app is not reachable
     */
    public fun startAppOnTheWatch(
        watchappUUID: UUID,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    )

    /**
     * Stop the app on the specified watches.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to stop
     * @param watches list of watches to start the app on to. If *null*, the message will be sent to all connected watches
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all requested watches
     *   or null if a Pebble app is not reachable
     *   (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    public fun stopAppOnTheWatch(
        watchappUUID: UUID,
        watches: List<WatchIdentifier>,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    )

    /**
     * Stop the app on the specified watches.
     *
     * This method does not need any special permissions from the watchapp, you can trigger it for every app.
     *
     * @param watchappUUID UUID of the app you want to stop
     * @param onResult Callback with the result. Supplied value is a map of transmission results for all requested watches
     *   or null if a Pebble app is not reachable
     *   (for example if not installed or limited by the [PebbleAndroidAppPicker])
     */
    public fun stopAppOnTheWatch(
        watchappUUID: UUID,
        onResult: Consumer<Map<WatchIdentifier, TransmissionResult>?>,
    )
}
