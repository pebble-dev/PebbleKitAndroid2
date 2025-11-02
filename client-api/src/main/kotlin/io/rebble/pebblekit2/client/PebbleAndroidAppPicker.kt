package io.rebble.pebblekit2.client

/**
 * Object that controls to which Pebble mobile app will PebbleKit Android connect.
 *
 * By default (with [enableAutoSelect] on), it will connect to any app. But if your application is sending
 * sensitive data over PebbleKit, a malicious app could pretend to be a Pebble Mobile app to extract information from
 * your app. In this case, set [enableAutoSelect] to false and manually call [selectApp], perhaps after user's selection.
 */
public interface PebbleAndroidAppPicker {
    public var enableAutoSelect: Boolean

    /**
     * Returns a package name of the currently selected app,
     * or *null* if none is selected.
     *
     * If [enableAutoSelect] is true, this method only returns *null* if there are no mobile Pebble apps installed.
     */
    public suspend fun getCurrentlySelectedApp(): String?

    /**
     * Set currently selected Pebble app. You can specify null to clear the selection.
     */
    public suspend fun selectApp(packageName: String?)

    /**
     * @return list of packages of all mobile Pebble apps currently installed.
     */
    public fun getAllEligibleApps(): List<String>
}
