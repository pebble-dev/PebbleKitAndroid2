package io.rebble.pebblekit2.client

import androidx.annotation.WorkerThread
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.model.ConnectedWatch
import io.rebble.pebblekit2.model.Watchapp
import kotlinx.coroutines.flow.Flow

public interface PebbleInfoRetriever {
    /**
     * Get a flow of currently connected watches. Flow will re-emit when watches change.
     *
     * if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker]),
     * it always returns an empty list
     */
    @WorkerThread
    public fun getConnectedWatches(): Flow<List<ConnectedWatch>>

    /**
     * Get a flow of a currently active app on the specified watch. Flow will re-emit when the app changes.
     *
     * if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker]),
     * it always returns null
     */
    @WorkerThread
    public fun getActiveApp(watch: WatchIdentifier): Flow<Watchapp?>
}
