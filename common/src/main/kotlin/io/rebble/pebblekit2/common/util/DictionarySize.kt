package io.rebble.pebblekit2.common.util

import io.rebble.pebblekit2.common.model.PebbleDictionary

/**
 * Get the serialized size of this dictionary (when set to the watch), in bytes.
 *
 * See https://developer.rebble.io/docs/c/Foundation/Dictionary/#dict_calc_buffer_size
 */
public fun PebbleDictionary.sizeInBytes(): Int {
    return 1 + values.sumOf { PEBBLE_DICTIONARY_TUPLE_HEADER_SIZE + it.size }
}

/**
 * Size of the serialized PebbleDictionary tuple header, in bytes.
 */
public const val PEBBLE_DICTIONARY_TUPLE_HEADER_SIZE: Int = 7
