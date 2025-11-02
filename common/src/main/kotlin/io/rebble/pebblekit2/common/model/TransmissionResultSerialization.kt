package io.rebble.pebblekit2.common.model

import android.os.Bundle

public fun TransmissionResult.Companion.fromBundle(bundle: Bundle): TransmissionResult {
    val type = bundle.getString(BUNDLE_KEY_TYPE)

    return when (type) {
        "FAILED_DIFFERENT_APP_OPEN" -> TransmissionResult.FailedDifferentAppOpen
        "FAILED_NO_PERMISSIONS" -> TransmissionResult.FailedNoPermissions
        "FAILED_TIMEOUT" -> TransmissionResult.FailedTimeout
        "FAILED_WATCH_NACKED" -> TransmissionResult.FailedWatchNacked
        "FAILED_WATCH_NOT_CONNECTED" -> TransmissionResult.FailedWatchNotConnected
        "SUCCESS" -> TransmissionResult.Success
        "UNKNOWN" -> TransmissionResult.Unknown(bundle.getString(BUNDLE_KEY_MESSAGE))
        else -> {
            co.touchlab.kermit.Logger.withTag("PebbleKit")
                .e { "Got unknown type ${type ?: "null"} while decoding TransmissionResult" }

            TransmissionResult.Unknown(type)
        }
    }
}

public fun TransmissionResult.toBundle(): Bundle {
    val bundle = Bundle()

    val type = when (this) {
        is TransmissionResult.FailedDifferentAppOpen -> "FAILED_DIFFERENT_APP_OPEN"
        is TransmissionResult.FailedNoPermissions -> "FAILED_NO_PERMISSIONS"
        is TransmissionResult.FailedTimeout -> "FAILED_TIMEOUT"
        is TransmissionResult.FailedWatchNacked -> "FAILED_WATCH_NACKED"
        is TransmissionResult.FailedWatchNotConnected -> "FAILED_WATCH_NOT_CONNECTED"
        is TransmissionResult.Success -> "SUCCESS"
        is TransmissionResult.Unknown -> {
            bundle.putString(BUNDLE_KEY_MESSAGE, message)
            "UNKNOWN"
        }
    }

    bundle.putString(BUNDLE_KEY_TYPE, type)
    return bundle
}

private const val BUNDLE_KEY_TYPE = "TYPE"
private const val BUNDLE_KEY_MESSAGE = "MESSAGE"
