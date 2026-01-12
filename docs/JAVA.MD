# Usage in Java projects

(See [Java sample app](../sample/android-java) for examples)

### Installation & Preparation

You should follow most of the steps of the installation and preparation of the regular library (described in the README),
except that you should use `client-java` instead of `client` dependency name:

`implementation("...:client-java:...")`

Here are described all the differences in Java projects from the regular procedure in the README:

### Sending data

To send data, use the `JavaPebbleSender` instead of the `PebbleSender`:

```java
JavaPebbleSender sender = new DefaultJavaPebbleSender(context);

Map<Integer, PebbleDictionaryItem> dictionary = new HashMap<>();
dictionary.put(1, new PebbleDictionaryItem.Text("Hello, Watch!");
dictionary.put(2, new PebbleDictionaryItem.UInt16(333));

sender.sendDataToPebble(
    APP_UUID,
    dictionary,
    (result) -> {
        // Handle transmission results here
    },
    List.of(new WatchIdentifier("my-watch"))
);
```

### Receive data

To receive data, your listener service has to extend `BaseJavaPebbleListenerService`. When receiving data,
you MUST call `responder.accept()` after you have processed your message:

```kotlin
public class PebbleListenerService extends BaseJavaPebbleListenerService {
    @Override
    public void onMessageReceived(
        @NotNull UUID watchappUUID,
        @NotNull Map<@NotNull Integer, ? extends @NotNull PebbleDictionaryItem> data,
        @NotNull String watch,
        @NotNull Consumer<@NotNull ReceiveResult> responder) {
    
        // Process your message here

        // Respond with either Ack or Nack
        responder.accept(ReceiveResult.Ack.INSTANCE); 
    }
}
```

### Selecting target Pebble app

Some `PebbleAndroidAppPicker` methods are not accessible from Java by default. To access them, you can call methods
on the `JavaPebbleAndroidAppPicker` wrapper:

```java
PebbleAndroidAppPicker picker = DefaultPebbleAndroidAppPicker.getInstance(this);

JavaPebbleAndroidAppPicker.getCurrentlySelectedApp(
    picker, 
    (result) -> {
        // Process currently selected app here
    }
);
```
