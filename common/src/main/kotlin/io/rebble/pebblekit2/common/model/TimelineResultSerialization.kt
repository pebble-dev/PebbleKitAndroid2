package io.rebble.pebblekit2.common.model

import android.os.Bundle
import co.touchlab.kermit.Logger

public fun TimelineResult.Companion.fromBundle(bundle: Bundle): TimelineResult {
    if (bundle.isEmpty) {
        return TimelineResult.FailedUnsupportedAction
    }

    val type = bundle.getString(BUNDLE_KEY_TYPE)

    return when (type) {
        "SUCCESS" -> TimelineResult.Success
        "FAILED_NO_PERMISSIONS" -> TimelineResult.FailedNoPermissions
        "UNKNOWN_PIN" -> TimelineResult.FailedUnknownPin
        "NO_PEBBLE_APP" -> TimelineResult.FailedNoPebbleApp
        "UNKNOWN" -> TimelineResult.Unknown(bundle.getString(BUNDLE_KEY_MESSAGE))
        else -> {
            Logger.withTag("PebbleKit")
                .e { "Got unknown type ${type ?: "null"} while decoding TimelineResult" }

            TimelineResult.Unknown(type)
        }
    }
}

public fun TimelineResult.toBundle(): Bundle {
    val bundle = Bundle()

    val type = when (this) {
        is TimelineResult.Success -> "SUCCESS"
        is TimelineResult.FailedNoPermissions -> "FAILED_NO_PERMISSIONS"
        is TimelineResult.FailedUnknownPin -> "UNKNOWN_PIN"
        TimelineResult.FailedNoPebbleApp -> "NO_PEBBLE_APP"
        is TimelineResult.FailedUnsupportedAction -> return bundle
        is TimelineResult.Unknown -> {
            bundle.putString(BUNDLE_KEY_MESSAGE, message)
            "UNKNOWN"
        }
    }

    bundle.putString(BUNDLE_KEY_TYPE, type)
    return bundle
}

private const val BUNDLE_KEY_TYPE = "TYPE"
private const val BUNDLE_KEY_MESSAGE = "MESSAGE"
