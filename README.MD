A work-in-progress implementation of the new modern PebbleKit API for communication between phone companion apps and
the watchapps on the Pebble-OS running watches.

Based
on [this design document](https://docs.google.com/document/d/1BcX7W9HEBays5puwcRQh1GzClyHc014IRzEIuk7lLuk/edit?tab=t.0).

# Usage

## Client-side (app) usage

(See [sample app](sample) for examples)

### Installation

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
implementation("com.github.pebble-dev.PebbleKitAndroid2:client:3dea822ae9")
```

[More detailed instructions](https://jitpack.io/#pebble-dev/PebbleKitAndroid2/client)

### Reference your app in the watchapp

In your watchapp's `package.json`, add a `companionApp` section:

```json
{
  "pebble": {
    "companionApp": {
      "android": {
        "url": "{LINK TO THE GITHUB/PLAY STORE WHERE USERS CAN GET THE COMPANION APP}",
        "apps": [
          {
            "package": "{COMPANION ANDROID APP PACKAGE}"
          }
        ]
      }
    }
  }
}
```

You can specify as many packages as you want, for example if you have debug and release variants. Pebble app will
connect to the first app on the list that is installed on the user's Android device

### Sending data

To send data to the watch, create an instance of the `PebbleSender`:

```kotlin
val sender = DefaultPebbleSender()
```

then craft your message (
see [Sending and receiving data](https://developer.rebble.io/guides/communication/sending-and-receiving-data/)
for more info on the message structure) by creating a map of tuples to send to the watch:

```kotlin
val dataToSend = mapOf(
    1u to PebbleDictionaryItem.String("Hello at ${LocalTime.now().toString()}"),
    2u to PebbleDictionaryItem.UInt8(10u),
)
```

and send it by calling the method on the `PebbleSender`:

`val result = sender.sendDataToPebble(APP_UUID, dataToSend)`

where the APP_UUID is the UUID of the target watchapp. Optionally, if you want to send data to a specific watch
(and the target Pebble app supports multiple connected watches), you can specify `watches` parameter of the
`sendDataToPebble method`.

The `result` will contain the result of the sending
(see [TransmissionResult](common-api/src/main/kotlin/io/rebble/pebblekit2/common/model/TransmissionResult.kt)).

`sendDataToPebble()` method is a suspending method and needs to be called from a coroutine. See
[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for more info.

when you are done with sending your messages, do not forget to call the close method:

`sender.close()`

### Receive data

First, create an implementation of the `BasePebbleListenerService`, where you override desired callbacks:

```kotlin
class PebbleListenerService : BasePebbleListenerService() {
    override suspend fun onMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier
    ): ReceiveResult {
        // ...
    }

    override fun onAppOpened(watchappUUID: UUID, watch: WatchIdentifier) {
        // ...
    }

    override fun onAppClosed(watchappUUID: UUID, watch: WatchIdentifier) {
        // ...
    }
}
```

Then, add the receiver as a service to your app's `AndroidManifest.xml`,
with the `io.rebble.pebblekit2.RECEIVE_DATA_FROM_WATCH` intent filter:

```xml
<service android:name="package.of.my.PebbleListenerService"
         android:exported="true">
    <intent-filter>
        <action android:name="io.rebble.pebblekit2.RECEIVE_DATA_FROM_WATCH"/>
    </intent-filter>
</service>
```

That's it. When your watchapp is opened on the watch, the listener service should be bounded and the
start callback called.

### Starting/stopping the app

You can call `sender.startAppOnTheWatch()` and `sender.stopAppOnTheWatch()` to start/stop your app on the watch
from the phone.

### Getting status about the Pebble watch

You can also use `PebbleInfoRetriever` to retrieve various infos about the status of the Pebble watch and its
connection.

Then you can use the `PebbleInfoRetriever` to get the data`:

```kotlin
val infoRetriever = DefaultPebbleInfoRetriever(this)

infoRetriever.getConnectedWatches()
infoRetriever.getActiveApp(watchId)
```

Alternatively, you can also use the content resolver directly:

```kotlin
val pebbleAppPackage = DefaultPebbleAndroidAppPicker.getCurrentlySelectedApp(context)

val cursor = contentResolver.query(
    PebbleKitProviderContract.ConnectedWatch.getContentUri(pebbleAppPackage),
    null,
    null,
    null,
    null
)
cursor?.use { cursor ->
    while (cursor.moveToNext()) {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(PebbleKitProviderContract.ConnectedWatch.ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(PebbleKitProviderContract.ConnectedWatch.NAME))
        // ...

    }
}
```

(Note that only the `projection` parameter is supported, other content resolver parameters are ignored)

### Reacting to the status about the Pebble watch in the background

`PebbleInfoRetriever` works only when the app is in the foreground. Unless your app has a service active all the time,
it will not be able to react to the changes in the background (for example,
to get a callback when a watch is (dis)connected while your app is in the background).

To do that, you can use Content URI monitoring in
the [JobScheduler](https://developer.android.com/reference/android/app/job/JobInfo.Builder#addTriggerContentUri(android.app.job.JobInfo.TriggerContentUri))
or in
the [WorkManager](https://developer.android.com/reference/androidx/work/Constraints.Builder#addContentUriTrigger(android.net.Uri,kotlin.Boolean)).

For example:

```kotlin
val pebbleAppPackageName = DefaultPebbleAndroidAppPicker.getInstance(context)
    .getCurrentlySelectedApp()

val triggerUri = PebbleKitProviderContract.ConnectedWatch.getContentUri(pebbleAppPackageName)

jobScheduler.schedule(
    JobInfo.Builder(myId, ComponentName(context, MyJobService::class.java))
        .addTriggerContentUri(JobInfo.TriggerContentUri(triggerUri, 0))
        .build()
)
```

above job will be triggered whenever a list of currently connected watches change, even if your app is sleeping in the
background at that time.

### Selecting target Pebble app

By default, this library will auto-connect and auto-accept requests from any Pebble app.
If your app is sending/receiving sensitive data, this might be a security risk as any app masquerading as a
Pebble app could be used to steal that data.

To mitigate for this, you can disable automatic selection and manually grant access to the specific Pebble app,
using `PebbleAndroidAppPicker` object.

First, you have to disable automatic selection:

`DefaultPebbleAndroidAppPicker.getInstance(context).enableAutoSelect = false`

Then, you can get a list of all installed Pebble apps:

`val apps = DefaultPebbleAndroidAppPicker.getInstance(context).getAllEligibleApps()`

finally, select the app that you want to grant access to:

`DefaultPebbleAndroidAppPicker.getInstance(context).selectApp(pebbleAppPackage)`

Now, PebbleKit will only talk to the app with the `pebbleAppPackage` package.

### Non-Kotlin usage

This library is written primarily with Kotlin use in mind. But we have a wrapper that allows it
to be used with non-Kotlin JVM languages, such as Java. See [JAVA.MD](docs/JAVA.MD).

## Logging

All non-fatal errors and warnings from the PebbleKit library are logged using the
[Kermit](https://kermit.touchlab.co) library.

By default, they are only logged to the Logcat, but this can be customized by writing custom Kermit
LogWriters.

## Server-side (Pebble mobile app) usage

See [SERVER.MD](docs/SERVER.MD).

# Supported Pebble apps

* [Pebble / Core app](https://github.com/coredevices/mobileapp) (from `1.0.7.7`)
* [microPebble](https://github.com/matejdro/microPebble) (from `1.0.0-alpha35`)

# Technical details

## IPC between apps

Old PebbleKit relied on the broadcast intents to transfer data between the Pebble Android app and the
companion app. This approach has two main weaknesses:

* Lax security: there is no way to know from which app the message originates. Malicious apps could
  pretend to be the either end of the conversation to extract data from either the Pebble app or
  the companion app
* Background work: in the years since the original Pebble app, Google has
  [step up its war on background processing](https://dontkillmyapp.com/). It is no longer easy to
  stay active the background to talk to the Pebble app. And broadcasts do not help with this
  as they cannot wake the sleeping app or keep it alive to talk to the watch.

As an alternative we picked
[Bound Services](https://developer.android.com/develop/background-work/services/bound-services). For this,
a service is created on the either side and the other side binds to this service. Most importantly,
while Pebble app is bound into the service of the companion app, this app is awakened and is kept
awake while the watch app is running, which should help with the background issues.

To exchange messages with bound services, Google gives us two options: AIDL and Messenger. Former
is a very nice exchange format, you declare interfaces and Google generates most of the code for you,
allowing for very easy calling. Unfortunately, the original format is not backwards or forwards compatible,
which would present challenges with evolving this library. Messenger does not have this issue
(it uses Bundles underneath), but it's very cumbersome to set up, especially for bidirectional communication.

In the end we went for a sort of hybrid. In the AIDL, we created a very simple interface that
[sends a bundle to a service and receives a callback with another bundle](common/src/main/aidl/io/rebble/pebblekit2/common/UniversalRequestResponse.aidl).
This interface is simple enough that we do not expect to change, so it can be used. On top of that, we use
Android's Bundles, which use keys and values underneath and can be made backwards compatible.

