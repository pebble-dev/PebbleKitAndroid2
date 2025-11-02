package io.rebble.pebblekit2.client

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import io.rebble.pebblekit2.PebbleKitProviderContract
import io.rebble.pebblekit2.PebbleKitProviderContract.ActiveApp
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.model.ConnectedWatch
import io.rebble.pebblekit2.model.Watchapp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Helper class that receives data from the [PebbleKitProviderContract] and maps it into objects
 */
public class DefaultPebbleInfoRetriever(private val context: Context) : PebbleInfoRetriever {
    /**
     * Get a flow of currently connected watches. Flow will re-emit when watches change.
     *
     * if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker]),
     * it always returns an empty list
     */
    @WorkerThread
    @Suppress("MagicNumber") // Array indices
    override fun getConnectedWatches(): Flow<List<ConnectedWatch>> {
        return queryPebbleProvider(
            getUri = { PebbleKitProviderContract.ConnectedWatch.getContentUri(it) },
            projection = PebbleKitProviderContract.ConnectedWatch.ALL_COLUMNS,
            mapper = { cursor ->
                ConnectedWatch(
                    WatchIdentifier(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getString(7),
                )
            }
        )
    }

    /**
     * Get a flow of a currently active app on the specified watch. Flow will re-emit when the app changes.
     *
     * if Pebble app is not reachable
     * (for example if not installed or limited by the [PebbleAndroidAppPicker]),
     * it always returns null
     */
    @WorkerThread
    @Suppress("MagicNumber") // Array indices
    override fun getActiveApp(watch: WatchIdentifier): Flow<Watchapp?> {
        return queryPebbleProvider(
            getUri = { ActiveApp.getContentUri(it, watch) },
            projection = ActiveApp.ALL_COLUMNS,
            mapper = { cursor ->
                Watchapp(
                    UUID.fromString(cursor.getString(0)),
                    cursor.getString(1),
                    when (cursor.getInt(2)) {
                        ActiveApp.TYPE_VALUE_WATCHFACE -> Watchapp.Type.WATCHFACE
                        ActiveApp.TYPE_VALUE_WATCHAPP -> Watchapp.Type.WATCHAPP
                        else -> Watchapp.Type.UNKNOWN
                    },
                )
            }
        ).map { it.firstOrNull() }
    }

    private fun <T> queryPebbleProvider(
        getUri: (packageName: String) -> Uri,
        projection: List<String>,
        mapper: (Cursor) -> T,
    ): Flow<List<T>> {
        return suspend {
            DefaultPebbleAndroidAppPicker.getInstance(context).getCurrentlySelectedApp()
        }.asFlow().flatMapConcat { pebbleAppPackage ->
            if (pebbleAppPackage == null) {
                flowOf(emptyList())
            } else {
                val uri = getUri(pebbleAppPackage)
                context.contentResolver.flowOfChanges(uri).map {
                    val cursor = context.contentResolver.query(
                        /* uri = */ uri,
                        /* projection = */ projection.toTypedArray(),
                        /* selection = */ null,
                        /* selectionArgs = */ null,
                        /* sortOrder = */ null,
                        /* cancellationSignal = */ null
                    )
                    cursor.use {
                        cursor?.mapToList(mapper).orEmpty()
                    }
                }
            }
        }
    }

    private fun ContentResolver.flowOfChanges(uri: Uri): Flow<Unit> {
        return channelFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            registerContentObserver(uri, false, observer)

            trySend(Unit)

            awaitClose {
                unregisterContentObserver(observer)
            }
        }
            .buffer(Channel.CONFLATED)
    }

    private fun <T> Cursor.mapToList(mapper: (Cursor) -> T): List<T> {
        return buildList {
            while (moveToNext()) {
                add(mapper(this@mapToList))
            }
        }
    }
}
