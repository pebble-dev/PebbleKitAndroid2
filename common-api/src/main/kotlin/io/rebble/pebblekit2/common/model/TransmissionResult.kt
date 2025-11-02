package io.rebble.pebblekit2.common.model

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

    public companion object
}
