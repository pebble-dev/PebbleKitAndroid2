package io.rebble.pebblekit2.common.model

public sealed class PebbleDictionaryItem {
    public abstract val value: Any

    public data class ByteArray(override val value: kotlin.ByteArray) : PebbleDictionaryItem() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ByteArray) return false

            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    public data class String(override val value: kotlin.String) : PebbleDictionaryItem()

    public data class Int8(override val value: Byte) : PebbleDictionaryItem()

    public data class UInt8(override val value: UByte) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Int) : this(value.toUByte())
    }

    public data class Int16(override val value: Short) : PebbleDictionaryItem()

    public data class UInt16(override val value: UShort) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Int) : this(value.toUShort())
    }

    public data class Int32(override val value: Int) : PebbleDictionaryItem()

    public data class UInt32(override val value: UInt) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Long) : this(value.toUInt())
    }

    public companion object
}

public typealias PebbleDictionary = Map<UInt, PebbleDictionaryItem>
