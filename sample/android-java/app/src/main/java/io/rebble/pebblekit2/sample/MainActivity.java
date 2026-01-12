package io.rebble.pebblekit2.sample;

import android.os.Bundle;

import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import io.rebble.pebblekit2.client.DefaultPebbleAndroidAppPicker;
import io.rebble.pebblekit2.client.PebbleAndroidAppPicker;
import io.rebble.pebblekit2.client.java.DefaultJavaPebbleSender;
import io.rebble.pebblekit2.client.java.JavaPebbleAndroidAppPicker;
import io.rebble.pebblekit2.client.java.JavaPebbleSender;
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem;
import io.rebble.pebblekit2.common.model.WatchIdentifier;
import kotlin.UInt;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private JavaPebbleSender sender = new DefaultJavaPebbleSender(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void sendTimeToWatch(View view) {
        Map<Integer, PebbleDictionaryItem> dictionary = new HashMap<>();
        dictionary.put(1, new PebbleDictionaryItem.Text("Hello at " + LocalTime.now()));
        dictionary.put(2, new PebbleDictionaryItem.UInt16(333));

        sender.sendDataToPebble(APP_UUID,
                dictionary,
                (result) -> {
                    System.out.println("Got result " + result);
                },
                List.of(new WatchIdentifier("my-watch"))
        );
    }

    private static final UUID APP_UUID = UUID.fromString("0054f75d-e60a-4932-8f8d-fe5c7dd365f6");
}

