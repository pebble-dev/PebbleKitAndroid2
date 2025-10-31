package io.rebble.pebblekit2.common.util

import android.content.Context
import android.os.Bundle
import io.rebble.pebblekit2.common.SendDataCallback
import io.rebble.pebblekit2.common.UniversalRequestResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public suspend fun UniversalRequestResponse.request(
    bundle: Bundle,
): Bundle = suspendCoroutine { cont ->
    val callback = object : SendDataCallback.Stub() {
        override fun onResult(bundle: Bundle) {
            cont.resume(bundle)
        }
    }

    request(bundle, callback)
}

public abstract class UniversalRequestResponseSuspending(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : UniversalRequestResponse.Stub() {
    final override fun request(data: Bundle, callback: SendDataCallback) {
        val callingPackage = context.packageManager.getNameForUid(getCallingUid())

        coroutineScope.launch {
            val result = request(data, callingPackage)
            callback.onResult(result)
        }
    }

    protected abstract suspend fun request(data: Bundle, callingPackage: String?): Bundle
}
