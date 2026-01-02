package io.rebble.pebblekit2.common.model

public sealed class PebbleDictionaryItem {
    public abstract val value: Any

    /**
     * Size of the value of this item, in bytes.
     */
    public abstract val size: Int

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

        override val size: Int
            get() = value.size
    }

    public data class String(override val value: kotlin.String) : PebbleDictionaryItem() {
        override val size: Int
            get() = value.toByteArray(Charsets.UTF_8).size
    }

    public data class Int8(override val value: Byte) : PebbleDictionaryItem() {
        override val size: Int
            get() = 1
    }

    public data class UInt8(override val value: UByte) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Int) : this(value.toUByte())

        override val size: Int
            get() = 1
    }

    public data class Int16(override val value: Short) : PebbleDictionaryItem() {
        override val size: Int
            get() = 2
    }

    public data class UInt16(override val value: UShort) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Int) : this(value.toUShort())

        override val size: Int
            get() = 2
    }

    public data class Int32(override val value: Int) : PebbleDictionaryItem() {
        override val size: Int
            get() = 4
    }

    public data class UInt32(override val value: UInt) : PebbleDictionaryItem() {
        // Extra constructor to allow creation from Java, without unsigned types
        public constructor(value: Long) : this(value.toUInt())

        override val size: Int
            get() = 4
    }

    public companion object
}

public typealias PebbleDictionary = Map<UInt, PebbleDictionaryItem>
