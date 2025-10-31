This document is for implementing server-side of the PebbleKit2 in a custom Pebble app.

# Installation

During the experimental phase, library is not yet pushed to the maven central. To useit , you will have to use git
submodules
to include it:

1. Download the repository using `git submodule add https://github.com/pebble-dev/PebbleKitAndroid2.git`    
   (If your project does not use git, you can just download this repo and put it into `PebbleKitAndroid2` folder)
2. Include build in your app's `settings.gradle(.kts)`:

```kotlin
includeBuild("PebbleKitAndroid2") {
    dependencySubstitution {
        substitute(module("io.rebble:pebblekit2-server"))
            .using(project(":server"))
    }
}
```

3. Add dependency in your app's `build.gradle(.kts)`: `implementation("io.rebble:pebblekit2-server:0.0.0")`

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
         android:permission="io.rebble.pebblekit2.permission.SEND_DATA_TO_WATCH">
    <intent-filter>
        <action android:name="io.rebble.pebblekit2.SEND_DATA_TO_WATCH"/>
    </intent-filter>
</service>
```

Do not forget to declare the permissions, which ensures that only apps with the sending
permission can connect to the watch.

3. That's it. Whenever companion app wants to send data to the watch, your `BasePebbleSenderReceiver`
   will be automatically constructed by the Android system and its suspending methods will be called.

Note: by default, suspending methods are called on the main coroutine scope, but you can change that by overriding the
`coroutineScope` of the `BasePebbleSenderReceiver`

# Sending data to apps

To send the data from the watch to the companion apps, you first a data reception permission:

`<uses-permission android:name="io.rebble.pebblekit2.permission.RECEIVE_DATA_FROM_WATCH" />`

then you need to create a `PebbleListenerConnector`, when a watchapp
with a companion is opened on the watch:

```kotlin
// targetPackages = the list of packages in the watchapp's package.json
private val connector = PebbleListenerConnector(context, targetPackages)
```

now you can call the functions for the appropriate callbacks, whenever events from the watch occur.

At the end, do not forget to close the connector. To ensure the companion app has some time to clean up after
the watchapp stopped event, close the connector with a delay of a few seconds:

```kotlin
delay(5.seconds)
connector.close()
```
