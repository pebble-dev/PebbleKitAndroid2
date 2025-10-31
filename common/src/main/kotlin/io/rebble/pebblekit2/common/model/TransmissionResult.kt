package io.rebble.pebblekit2.common.model

import android.os.Bundle
import co.touchlab.kermit.Logger

public sealed class TransmissionResult {

    /**
     * Data was sent successfully
     */
    public data object Success : TransmissionResult()

    /**
     * Watch is not currently connected
     */
    public data object FailedWatchNotConnected : TransmissionResult()

    /**
     * Message was delivered to the watch, but the watch replied with the NACK.
     */
    public data object FailedWatchNacked : TransmissionResult()

    /**
     * There was a timeout while sending a message to the watch
     */
    public data object FailedTimeout : TransmissionResult()

    /**
     * A different app is currently open on the watch than the app you wanted to send the data to. Only
     * foreground Pebble app can send and receive AppMessages.
     */
    public data object FailedDifferentAppOpen : TransmissionResult()

    /**
     * This companion app does not have the permissions to send the data to the target watch app. This happens
     * when this app is not added to the list of android packages in the watchapp's package.json.
     */
    public data object FailedNoPermissions : TransmissionResult()

    /**
     * Result of the transmission was unknown. This usually signifies that app uses an outdated version of the
     * PebbleKit library.
     *
     * @property message might tell more info.
     */
    public data class Unknown(val message: String? = null) : TransmissionResult()

    public companion object {
        public fun fromBundle(bundle: Bundle): TransmissionResult {
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
                    Logger.withTag("PebbleKit")
                        .e { "Got unknown type ${type ?: "null"} while decoding TransmissionResult" }

                    TransmissionResult.Unknown(type)
                }
            }
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
