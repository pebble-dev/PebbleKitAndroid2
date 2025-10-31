package io.rebble.pebblekit2.server

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.annotation.CallSuper
import io.rebble.pebblekit2.PebbleKitProviderContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

/**
 * Base for the PebbleKit content provider.
 *
 * After your app has initialized, you MUST call [initialize] method, which will start all the listeners
 * (this has to be done manually due to the lifecycle of the Content Provider
 * that usually starts before everything else)
 */
public abstract class BasePebbleKitProvider : ContentProvider() {
    protected open val coroutineScope: CoroutineScope = MainScope()

    private val initializedLatch = CountDownLatch(1)

    @Volatile
    private var connectedWatchesState: List<Map<String, Any?>> = emptyList()

    /**
     * Initializer. This method must be called exactly once, after your application has initialized. All callbacks
     * in this class will only be called after initialization
     */
    @CallSuper
    public open fun initialize() {
        coroutineScope.launch {
            getConnectedWatches()
                .distinctUntilChanged()
                .collect {
                    connectedWatchesState = it

                    requireNotNull(context).contentResolver.notifyChange(
                        PebbleKitProviderContract.ConnectedWatch.getContentUri(requireNotNull(context).packageName),
                        null
                    )
                }
        }

        initializedLatch.countDown()
    }

    /**
     * Get the flow of the currently connected watches.
     *
     * It must contain the list of map objects that contain mappings of all columns,
     * listed in the [PebbleKitProviderContract.ConnectedWatch].
     */
    protected abstract fun getConnectedWatches(): Flow<List<Map<String, Any?>>>

    override fun onCreate(): Boolean {
        // Do not initialize anything here as this gets called before Application.onCreate, so the server app likely
        // is not ready to serve the requests yet

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?,
    ): Cursor? {
        if (uri.authority != PebbleKitProviderContract.getAuthority(requireNotNull(context).packageName)) {
            return null
        }

        initializedLatch.await()

        return if (uri.path?.removePrefix("/") == PebbleKitProviderContract.ConnectedWatch.CONTENT_PATH) {
            queryConnectedWatches(projection)
        } else {
            null
        }
    }

    private fun queryConnectedWatches(projection: Array<out String?>?): Cursor {
        val sentColumns =
            projection?.filterNotNull()?.filter { column ->
                PebbleKitProviderContract.ConnectedWatch.ALL_COLUMNS.contains(column)
            }
                ?: PebbleKitProviderContract.ConnectedWatch.ALL_COLUMNS

        val cursor = MatrixCursor(sentColumns.toTypedArray())

        cursor.use { _ ->
            val watches = connectedWatchesState
            for (watch in watches) {
                val values = sentColumns.map { watch.getValue(it) }.toTypedArray()
                cursor.addRow(values)
            }
        }

        return cursor
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int {
        return 0
    }
}
