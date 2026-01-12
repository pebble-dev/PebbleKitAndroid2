package io.rebble.pebblekit2.common.model

import android.os.Bundle
import co.touchlab.kermit.Logger

public fun PebbleDictionaryItem.Companion.mapFromBundle(bundle: Bundle): PebbleDictionary {
    return bundle.keySet().filter { it.endsWith("_TYPE") }.mapNotNull { typeKey ->
        val dictKey = typeKey.substringBefore("_TYPE").toUInt()
        val valueKey = "${dictKey}_VALUE"
        val type = bundle.getString(typeKey)
        when (type) {
            "ByteArray" -> {
                bundle.getByteArray(valueKey)?.let {
                    dictKey to PebbleDictionaryItem.Bytes(it)
                }
            }

            "String" -> {
                bundle.getString(valueKey)?.let {
                    dictKey to PebbleDictionaryItem.Text(it)
                }
            }

            "Int16" -> {
                dictKey to PebbleDictionaryItem.Int16(bundle.getLong(valueKey).toShort())
            }

            "Int32" -> {
                dictKey to PebbleDictionaryItem.Int32(bundle.getLong(valueKey).toInt())
            }

            "Int8" -> {
                dictKey to PebbleDictionaryItem.Int8(bundle.getLong(valueKey).toByte())
            }

            "UInt16" -> {
                dictKey to PebbleDictionaryItem.UInt16(bundle.getLong(valueKey).toUShort())
            }

            "UInt32" -> {
                dictKey to PebbleDictionaryItem.UInt32(bundle.getLong(valueKey).toUInt())
            }

            "UInt8" -> {
                dictKey to PebbleDictionaryItem.UInt8(bundle.getLong(valueKey).toUByte())
            }

            else -> {
                Logger.withTag("PebbleKit")
                    .e { "Got unknown type ${type ?: "null"} while decoding PebbleDictionary" }
                null
            }
        }
    }.toMap()
}

public fun PebbleDictionary.toBundle(): Bundle {
    return Bundle().apply {
        for (entry in entries) {
            val key = entry.key.toString()
            when (val value = entry.value) {
                is PebbleDictionaryItem.Bytes -> {
                    putString("${key}_TYPE", "ByteArray")
                    putByteArray("${key}_VALUE", value.value)
                }

                is PebbleDictionaryItem.Text -> {
                    putString("${key}_TYPE", "String")
                    putString("${key}_VALUE", value.value)
                }

                is PebbleDictionaryItem.Int16 -> {
                    putString("${key}_TYPE", "Int16")
                    putLong("${key}_VALUE", value.value.toLong())
                }

                is PebbleDictionaryItem.Int32 -> {
                    putString("${key}_TYPE", "Int32")
                    putLong("${key}_VALUE", value.value.toLong())
                }

                is PebbleDictionaryItem.Int8 -> {
                    putString("${key}_TYPE", "Int8")
                    putLong("${key}_VALUE", value.value.toLong())
                }

                is PebbleDictionaryItem.UInt16 -> {
                    putString("${key}_TYPE", "UInt16")
                    putLong("${key}_VALUE", value.value.toLong())
                }

                is PebbleDictionaryItem.UInt32 -> {
                    putString("${key}_TYPE", "UInt32")
                    putLong("${key}_VALUE", value.value.toLong())
                }

                is PebbleDictionaryItem.UInt8 -> {
                    putString("${key}_TYPE", "UInt8")
                    putLong("${key}_VALUE", value.value.toLong())
                }
            }
        }
    }
}
