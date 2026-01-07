This document is for implementing server-side of the PebbleKit2 in a custom Pebble app.

# Installation

During the experimental phase, library is not yet pushed to the maven central. To use it, you will have to use Jitlpack
to include it.

First add Jitpack to your maven repositories:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Then you can include it like this:

```kotlin
implementation("com.github.pebble-dev.PebbleKitAndroid2:server:510b3ead6e")
```

[More detailed instructions](https://jitpack.io/#pebble-dev/PebbleKitAndroid2/server)

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
         android:exported="true">
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
