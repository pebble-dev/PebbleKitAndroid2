package io.rebble.pebblekit2.common.model

public sealed class ReceiveResult {

    public data object Ack : ReceiveResult()
    public data object Nack : ReceiveResult()

    /**
     * Result of the receiving was unknown. This usually signifies that app uses an outdated version of the
     * PebbleKit library.
     *
     * @property message might tell more info.
     */
    public data class Unknown(val message: String? = null) : ReceiveResult()

    public companion object
}
