This document is for implementing server-side of the PebbleKit2 in a custom Pebble app.

# Installation

```kotlin
implementation("io.rebble.pebblekit2:server:X.Y.Z")
```

Replace `X.Y.Z` with latest version. Check out [releases](https://github.com/pebble-dev/PebbleKitAndroid2/releases) to
see what is the latest
version.

# Receiving data from apps

To receive data from the companion apps (and forward it to the watch), you have to:

1. Make a subclass of the `BasePebbleSenderReceiver`:

```kotlin
class PebbleSenderReceiver : BasePebbleSenderReceiver() {
    // ...
}
```

2. Add the receiver as a service to your app's `AndroidManifest.xml`,
   with the `io.rebble.pebblekit2.SEND_DATA_TO_WATCH` intent filter:

```xml

<service android:name="package.of.my.PebbleSenderReceiver" 
         android:exported="true"
         tools:ignore="ExportedService">
    <intent-filter>
        <action android:name="io.rebble.pebblekit2.SEND_DATA_TO_WATCH"/>
    </intent-filter>
</service>
```

3. That's it. Whenever companion app wants to send data to the watch, your `BasePebbleSenderReceiver`
   will be automatically constructed by the Android system and its suspending methods will be called.

Note: by default, suspending methods are called on the main coroutine scope, but you can change that by overriding the
`coroutineScope` of the `BasePebbleSenderReceiver`

# Sending data to apps

To send the data from the watch to the companion apps, you need to create a `PebbleListenerConnector`, when a watchapp
with a companion is opened on the watch:

```kotlin
// targetPackages = the list of packages in the watchapp's package.json
private val connector = DefaultPebbleListenerConnector(context, targetPackages)
```

now you can call the functions for the appropriate callbacks, whenever events from the watch occur.

At the end, do not forget to close the connector. To ensure the companion app has some time to clean up after
the watchapp stopped event, close the connector with a delay of a few seconds:

```kotlin
delay(5.seconds)
connector.close()
```

# Forward data logging to apps

The watch sends data logging items of third-party watchapps (see
[Datalogging](https://developer.rebble.io/guides/communication/datalogging/)). Forward each batch to
the companion app of the watchapp through the same `PebbleListenerConnector`:

```kotlin
val result = connector.sendOnDataLogReceived(watchappUUID, session, data, itemsLeft, watch)
```

A batch must contain only whole items: `data.size` must be a multiple of `session.itemSize`. Send
the batches of one session in sequence: await the result of a call before you send the next batch
of that session. Keep one call well below the 1 MB Android binder transaction limit; do not put
more than 100 KB of data into one call.

The watch closes a session after it sent all of its data. When the companion app has acknowledged
all the batches of the session, call:

```kotlin
val result = connector.sendOnDataLogSessionFinished(watchappUUID, session, watch)
```

Data logging is different from messages: it is not connected to an open watchapp. The watch sends
stored data each time it is connected. The bind operation wakes the companion app. Thus make these
calls also outside of the app opened/closed window.

The result tells you what to do with the data. `ReceiveResult.Ack` means that the companion app
stored the data. You can then discard it. `ReceiveResult.Nack`, `ReceiveResult.Unknown` (the
companion app has an old PebbleKit library) and `null` (no connection to the app) mean that the
companion app did not store the data. Keep the data and send it again later, a limited number of
times. Discard the data when the attempts are used up or when the companion app is no longer
installed; do not retry without a limit. The companion app must tolerate a batch that it gets more
than one time, so a retry after an unclear result is safe.

The watch deletes its copy when the Pebble app confirms the receipt on the Pebble protocol level.
Data that the companion app did not acknowledge lives only in the Pebble app; treat it with care.

# Implementing status provider

To allow apps to access various infos about the status of the watch and the connection, you have to implement the
content provider.

To do that, first make a subclass of the `BasePebbleKitProvider`:

```kotlin
class PebbleKitProvider : BasePebbleKitProvider() {
    // ...
}
```

Then, in your `Application.onCreate()` class, call `initialize()` method on the provider, after all of your
logic is initialized and ready:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        // ...
        
        pebbleKitProvider.initialize()
    }
}
```

Finally, add the provider to the manifest:

```xml
<provider
    android:authorities="my.app.pebblekit"
    android:name="package.of.my.PebbleKitProvider"
    android:exported="true" />
```

Note that the authority must be `YOUR_APP_PACKAGE.pebblekit`.
