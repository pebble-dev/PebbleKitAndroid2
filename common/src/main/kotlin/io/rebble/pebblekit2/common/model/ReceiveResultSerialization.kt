package io.rebble.pebblekit2.common.model

import android.os.Bundle
import co.touchlab.kermit.Logger

public fun ReceiveResult.Companion.fromBundle(bundle: Bundle): ReceiveResult {
    val type = bundle.getString(BUNDLE_KEY_TYPE)

    return when (type) {
        "ACK" -> ReceiveResult.Ack
        "NACK" -> ReceiveResult.Nack
        "UNKNOWN" -> ReceiveResult.Unknown(bundle.getString(BUNDLE_KEY_MESSAGE))
        else -> {
            Logger.withTag("PebbleKit")
                .e { "Got unknown type ${type ?: "null"} while decoding ReceiveResult" }

            ReceiveResult.Unknown(type)
        }
    }
}

public fun ReceiveResult.toBundle(): Bundle {
    val bundle = Bundle()

    val type = when (this) {
        ReceiveResult.Ack -> "ACK"
        ReceiveResult.Nack -> "NACK"
        is ReceiveResult.Unknown -> {
            bundle.putString(BUNDLE_KEY_MESSAGE, message)
            "UNKNOWN"
        }
    }

    bundle.putString(BUNDLE_KEY_TYPE, type)
    return bundle
}

private const val BUNDLE_KEY_TYPE = "TYPE"
private const val BUNDLE_KEY_MESSAGE = "MESSAGE"
