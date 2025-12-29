package io.rebble.pebblekit2.sample;

import android.util.Log;
import io.rebble.pebblekit2.client.java.BaseJavaPebbleListenerService;
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem;
import io.rebble.pebblekit2.common.model.ReceiveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PebbleListenerService extends BaseJavaPebbleListenerService {
    @Override
    public void onMessageReceived(@NotNull UUID watchappUUID,
                                  @NotNull Map<@NotNull Integer, ? extends @NotNull PebbleDictionaryItem> data,
                                  @NotNull String watch,
                                  @NotNull Consumer<@NotNull ReceiveResult> responder) {
        Log.d("PebbleListenerService", "Received " + data + " from app " + watchappUUID + " on the watch " + watch);

        responder.accept(ReceiveResult.Ack.INSTANCE);
    }

    @Override
    protected void onAppOpened(@NotNull UUID watchappUUID, @NotNull String watch) {
        Log.d("PebbleListenerService", "App " + watchappUUID + " on the watch " + watch + " opened");
    }

    @Override
    protected void onAppClosed(@NotNull UUID watchappUUID, @NotNull String watch) {
        Log.d("PebbleListenerService", "App " + watchappUUID + " on the watch " + watch + " closed");
    }
}
