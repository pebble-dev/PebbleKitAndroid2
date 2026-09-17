package io.rebble.pebblekit2.common.util

import android.content.Context
import android.os.Bundle
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import co.touchlab.kermit.Logger
import io.rebble.pebblekit2.PebbleKitBundleKeys
import io.rebble.pebblekit2.common.SendDataCallback
import io.rebble.pebblekit2.common.UniversalRequestResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

public suspend fun UniversalRequestResponse.request(
    bundle: Bundle,
): Bundle? {
    val binder = asBinder()
    val result = CompletableDeferred<Bundle?>()
    val deathRecipient = IBinder.DeathRecipient { result.complete(null) }
    val callback = object : SendDataCallback.Stub() {
        override fun onResult(bundle: Bundle) {
            result.complete(bundle)
        }
    }

    try {
        binder.linkToDeath(deathRecipient, 0)
    } catch (ignored: RemoteException) {
        // Already dead
        return null
    }
    try {
        request(bundle, callback)
    } catch (ignored: DeadObjectException) {
        result.complete(null)
    }
    try {
        return result.await()
    } finally {
        runCatching { binder.unlinkToDeath(deathRecipient, 0) }
    }
}

public abstract class UniversalRequestResponseSuspending(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : UniversalRequestResponse.Stub() {
    final override fun request(data: Bundle, callback: SendDataCallback) {
        val callingPackage = context.packageManager.getNameForUid(getCallingUid())

        coroutineScope.launch {
            val result = try {
                request(data, callingPackage)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val action = data.getString(PebbleKitBundleKeys.KEY_ACTION) ?: "UNKNOWN"
                Logger.withTag("PebbleKit2").e(e) { "Failed to process '$action' bundle" }
                Bundle()
            }

            try {
                callback.onResult(result)
            } catch (ignored: DeadObjectException) {
                // Do nothing, callback is not needed if the other side crashed
            }
        }
    }

    protected abstract suspend fun request(data: Bundle, callingPackage: String?): Bundle
}
