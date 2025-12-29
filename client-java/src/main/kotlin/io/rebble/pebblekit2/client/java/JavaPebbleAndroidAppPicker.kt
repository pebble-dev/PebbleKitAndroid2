package io.rebble.pebblekit2.client.java

import io.rebble.pebblekit2.client.PebbleAndroidAppPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.function.Consumer

public object JavaPebbleAndroidAppPicker {
    /**
     * Get a package name of the currently selected app,
     * or *null* if none is selected.
     *
     * Result is passed into [result] callback.
     *
     * If [enableAutoSelect] is true, this method only returns *null* if there are no mobile Pebble apps installed.
     */
    @JvmStatic
    @JvmOverloads
    public fun getCurrentlySelectedApp(
        pebbleAndroidAppPicker: PebbleAndroidAppPicker,
        result: Consumer<String?>,
        coroutineScope: CoroutineScope = MainScope(),
    ) {
        coroutineScope.launch {
            val out = pebbleAndroidAppPicker.getCurrentlySelectedApp()
            result.accept(out)
        }
    }

    /**
     * Set currently selected Pebble app. You can specify null to clear the selection.
     *
     * You can pass in [completed] callback to get the callback after she selection is completed.
     */
    @JvmOverloads
    @JvmStatic
    public fun selectApp(
        pebbleAndroidAppPicker: PebbleAndroidAppPicker,
        packageName: String?,
        completed: Runnable? = null,
        coroutineScope: CoroutineScope = MainScope(),
    ) {
        coroutineScope.launch {
            pebbleAndroidAppPicker.selectApp(packageName)
            completed?.run()
        }
    }
}
