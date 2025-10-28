package io.rebble.pebblekit2.common.util

import android.os.Bundle
import io.rebble.pebblekit2.common.SendDataCallback
import io.rebble.pebblekit2.common.UniversalRequestResponse
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
