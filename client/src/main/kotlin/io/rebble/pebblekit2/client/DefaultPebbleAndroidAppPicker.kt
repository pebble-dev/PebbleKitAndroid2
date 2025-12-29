package io.rebble.pebblekit2.client

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.rebble.pebblekit2.common.PebbleKitIntents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * Object that controls to which Pebble mobile app will PebbleKit Android connect.
 *
 * By default (with [enableAutoSelect] on), it will connect to any app. But if your application is sending
 * sensitive data over PebbleKit, a malicious app could pretend to be a Pebble Mobile app to extract information from
 * your app. In this case, set [enableAutoSelect] to false and manually call [selectApp], perhaps after user's selection.
 */
public class DefaultPebbleAndroidAppPicker private constructor(private val context: Context) : PebbleAndroidAppPicker {
    override var enableAutoSelect: Boolean = true

    private var preferences: DataStore<Preferences>? = null

    /**
     * Returns a package name of the currently selected app,
     * or *null* if none is selected.
     *
     * If [enableAutoSelect] is true, this method only returns *null* if there are no mobile Pebble apps installed.
     */
    override suspend fun getCurrentlySelectedApp(): String? {
        val allEligibleApps = getAllEligibleApps()
        if (allEligibleApps.isEmpty()) return null

        val currentlySelectedValue = createOrGetDataStore(context).data.first()[SELECTED_APP_KEY]
            .takeIf { allEligibleApps.contains(it) }

        return if (currentlySelectedValue != null || !enableAutoSelect) {
            currentlySelectedValue
        } else {
            allEligibleApps.first()
        }
    }

    /**
     * Set currently selected Pebble app. You can specify null to clear the selection.
     */
    override suspend fun selectApp(packageName: String?) {
        require(packageName == null || getAllEligibleApps().contains(packageName)) {
            "Package ${packageName ?: "null"} is not a mobile Pebble app"
        }

        createOrGetDataStore(context).edit {
            if (packageName == null) {
                it.remove(SELECTED_APP_KEY)
            } else {
                it[SELECTED_APP_KEY] = packageName
            }
        }
    }

    /**
     * @return list of packages of all mobile Pebble apps currently installed.
     */
    override fun getAllEligibleApps(): List<String> {
        return context.packageManager.queryIntentServices(Intent(PebbleKitIntents.SEND_DATA), 0)
            .map { it.serviceInfo.packageName }
            .distinct()
    }

    @Suppress("InjectDispatcher") // We are forced to use hardcoded dispatcher here as this is a singleton
    private fun createOrGetDataStore(context: Context): DataStore<Preferences> {
        preferences?.let { return it }

        return synchronized(this) {
            val newInstance = preferences ?: PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO)) {
                context.preferencesDataStoreFile("pebblekit_android_app_picker")
            }

            this.preferences = newInstance

            newInstance
        }
    }

    public companion object {
        private var instance: PebbleAndroidAppPicker? = null

        @JvmStatic
        public fun getInstance(context: Context): PebbleAndroidAppPicker {
            instance?.let { return it }

            synchronized(this) {
                instance?.let { return it }

                val newInstance = DefaultPebbleAndroidAppPicker(context.applicationContext)
                instance = newInstance
                return newInstance
            }
        }
    }
}

private val SELECTED_APP_KEY = stringPreferencesKey("selected_app")
