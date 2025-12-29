package io.rebble.pebblekit2

import android.content.ContentResolver
import android.net.Uri
import io.rebble.pebblekit2.PebbleKitProviderContract.ActiveApp.TYPE_VALUE_UNKNOWN
import io.rebble.pebblekit2.PebbleKitProviderContract.ActiveApp.TYPE_VALUE_WATCHAPP
import io.rebble.pebblekit2.PebbleKitProviderContract.ActiveApp.TYPE_VALUE_WATCHFACE
import io.rebble.pebblekit2.common.model.WatchIdentifier

/**
 * The contract between the PebbleKit content provider and applications.
 *
 * Note that the *selection* and the *sortOrder* arguments during the content resolving are not supported.
 */
public object PebbleKitProviderContract {
    /**
     * The authority for the PebbleKit provider
     */
    @JvmStatic
    public fun getAuthority(packageName: String): String {
        return "$packageName.pebblekit"
    }

    /**
     * A content:// style uri to the authority for the PebbleKIt provider
     */
    @JvmStatic
    public fun getAuthorityUri(packageName: String): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(getAuthority(packageName))
            .build()
    }

    /**
     * Constants for the connected watches table, which contains a list of currently connected watches and basic metadata for them
     */
    public object ConnectedWatch {
        /**
         * ID of the watch.
         *
         * Column type: String (corresponds to the [WatchIdentifier])
         */
        public const val ID: String = "ID"

        /**
         * Display name of the watch, possibly user-edited.
         *
         * Column type: String
         */
        public const val NAME: String = "NAME"

        /**
         * Platform of the watch such as `aplite`, `basalt` etc.
         *
         * See the [Rebble documentation](https://developer.rebble.io/guides/tools-and-resources/hardware-information)
         * for the full list of the platforms
         *
         * Column type: String
         */
        public const val PLATFORM: String = "PLATFORM"

        /**
         * Hardware revision
         *
         * Column type: String
         */
        public const val REVISION: String = "REVISION"

        /**
         * Major part of the firmware version.
         *
         * Column type: Integer
         */
        public const val FIRMWARE_VERSION_MAJOR: String = "FIRMWARE_VERSION_MAJOR"

        /**
         * Minor part of the firmware version.
         *
         * Column type: Integer
         */
        public const val FIRMWARE_VERSION_MINOR: String = "FIRMWARE_VERSION_MINOR"

        /**
         * Patch part of the firmware version.
         *
         * Column type: Integer
         */
        public const val FIRMWARE_VERSION_PATCH: String = "FIRMWARE_VERSION_PATCH"

        /**
         * Tag part of the firmware version.
         *
         * Column type: String
         */
        public const val FIRMWARE_VERSION_TAG: String = "FIRMWARE_VERSION_TAG"

        @JvmStatic
        public val ALL_COLUMNS: List<String> = listOf(
            ID,
            NAME,
            PLATFORM,
            REVISION,
            FIRMWARE_VERSION_MAJOR,
            FIRMWARE_VERSION_MINOR,
            FIRMWARE_VERSION_PATCH,
            FIRMWARE_VERSION_TAG
        )

        /**
         * Path suffix to access this table
         */
        public const val CONTENT_PATH: String = "connectedWatches"

        /**
         * The content:// style URI for this table
         */
        @JvmStatic
        public fun getContentUri(packageName: String): Uri {
            return Uri.withAppendedPath(getAuthorityUri(packageName), CONTENT_PATH)
        }
    }

    /**
     * Constants for the active app table, which returns at most a single row of data that corresponds
     * to the current active app on the specified watch
     * (or an empty cursor if the active app is unknown or this watch is not connected)
     */
    public object ActiveApp {
        /**
         * UUID of the app.
         *
         * Column type: String
         */
        public const val ID: String = "ID"

        /**
         * Name of the app (can be null if unknown)
         *
         * Column type: String
         */
        public const val NAME: String = "NAME"

        /**
         * The type of the app
         *
         * Column type: Int (either [TYPE_VALUE_WATCHFACE], [TYPE_VALUE_WATCHAPP] or [TYPE_VALUE_UNKNOWN])
         */
        public const val TYPE: String = "TYPE"
        public const val TYPE_VALUE_WATCHFACE: Int = 0
        public const val TYPE_VALUE_WATCHAPP: Int = 1
        public const val TYPE_VALUE_UNKNOWN: Int = 2

        @JvmStatic
        public val ALL_COLUMNS: List<String> = listOf(
            ID,
            NAME,
            TYPE
        )

        /**
         * Path suffix to access this table
         */
        public const val CONTENT_PATH: String = "activeApp"

        /**
         * The content:// style URI for this table
         */
        @JvmStatic
        public fun getContentUri(packageName: String, watch: WatchIdentifier): Uri {
            val baseUri = Uri.withAppendedPath(getAuthorityUri(packageName), CONTENT_PATH)

            return Uri.withAppendedPath(baseUri, watch.value)
        }
    }
}
