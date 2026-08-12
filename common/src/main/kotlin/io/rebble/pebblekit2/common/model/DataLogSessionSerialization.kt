package io.rebble.pebblekit2.common.model

import android.os.Bundle

public fun DataLogSession.Companion.fromBundle(bundle: Bundle): DataLogSession {
    return DataLogSession(
        tag = bundle.getLong(BUNDLE_KEY_TAG),
        timestamp = bundle.getLong(BUNDLE_KEY_TIMESTAMP),
        itemSize = bundle.getInt(BUNDLE_KEY_ITEM_SIZE),
    )
}

public fun DataLogSession.toBundle(): Bundle {
    val bundle = Bundle()

    bundle.putLong(BUNDLE_KEY_TAG, tag)
    bundle.putLong(BUNDLE_KEY_TIMESTAMP, timestamp)
    bundle.putInt(BUNDLE_KEY_ITEM_SIZE, itemSize)

    return bundle
}

private const val BUNDLE_KEY_TAG = "TAG"
private const val BUNDLE_KEY_TIMESTAMP = "TIMESTAMP"
private const val BUNDLE_KEY_ITEM_SIZE = "ITEM_SIZE"
