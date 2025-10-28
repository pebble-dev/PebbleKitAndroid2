# Usage

# Logging

All non-fatal errors and warnings from the PebbleKit library are logged using the
[Kermit](https://kermit.touchlab.co) library.

By default, they are only logged to the Logcat, but this can be customized by writing custom Kermit
LogWriters.

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

