package io.rebble.pebblekit2.common.model

public sealed class TimelineResult {

    /**
     * Data was sent successfully
     */
    public data object Success : TimelineResult()

    /**
     * This companion app does not have the permissions to send the data to the target watch app. This happens
     * when this app is not added to the list of android packages in the watchapp's package.json.
     */
    public data object FailedNoPermissions : TimelineResult()

    /**
     * Installed Pebble app does not support timeline sending via the PebbleKit 2.
     */
    public data object FailedUnsupportedAction : TimelineResult()

    /**
     * Pin with the provided ID does not exist
     */
    public data object FailedUnknownPin : TimelineResult()

    /**
     * Pebble app is not installed, not reachable or blocked by the *PebbleAndroidAppPicker*.
     */
    public data object FailedNoPebbleApp : TimelineResult()

    /**
     * Result of the transmission was unknown. This usually signifies that app uses an outdated version of the
     * PebbleKit library.
     *
     * @property message might tell more info.
     */
    public data class Unknown(val message: String? = null) : TimelineResult()

    public companion object
}
