package io.rebble.pebblekit2.sample

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import io.rebble.pebblekit2.PebbleKitProviderContract
import io.rebble.pebblekit2.client.DefaultPebbleAndroidAppPicker
import io.rebble.pebblekit2.client.DefaultPebbleInfoRetriever
import io.rebble.pebblekit2.client.DefaultPebbleSender
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.sample.ui.theme.PebbleKitSampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val sender = DefaultPebbleSender(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(); setContent {
            PebbleKitSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SampleUI(
                        modifier = Modifier.fillMaxSize(),
                        sendTimeToWatch = ::sendTimeToWatch,
                        openApp = ::openAppOnWatch,
                        closeApp = ::closeAppOnWatch,
                        connectedWatchesForeground = ::startGettingConnectedWatchesInForeground,
                        connectedWatchesBackground = ::startGettingConnectedWatchesInBackground,
                    )
                }
            }
        }
    }

    private fun sendTimeToWatch() {
        lifecycleScope.launch {
            val result = sender.sendDataToPebble(
                APP_UUID,
                mapOf(1u to PebbleDictionaryItem.String("Hello at ${LocalTime.now()}"))
            )

            Log.d("PebbleKitSample", "Message sent. Result: $result")
        }
    }

    private fun openAppOnWatch() {
        lifecycleScope.launch {
            val result = sender.startAppOnTheWatch(APP_UUID)

            Log.d("PebbleKitSample", "Command sent. Result: $result")
        }
    }

    private fun closeAppOnWatch() {
        lifecycleScope.launch {
            val result = sender.stopAppOnTheWatch(APP_UUID)

            Log.d("PebbleKitSample", "Command sent. Result: $result")
        }
    }

    private fun startGettingConnectedWatchesInForeground() {
        val infoRetriever = DefaultPebbleInfoRetriever(this)

        lifecycleScope.launch {
            infoRetriever.getConnectedWatches()
                .flowOn(Dispatchers.Default)
                .collect {
                    Log.d("PebbleKitSample", "Connected watches update: $it")
                }
        }
    }

    private fun startGettingConnectedWatchesInBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        lifecycleScope.launch {

            val jobScheduler = getSystemService<JobScheduler>()!!

            val pebbleAppPackageName = DefaultPebbleAndroidAppPicker.getInstance(this@MainActivity)
                .getCurrentlySelectedApp()
                ?: return@launch

            val triggerUri = PebbleKitProviderContract.ConnectedWatch.getContentUri(pebbleAppPackageName)
            jobScheduler.schedule(
                JobInfo.Builder(100, ComponentName(this@MainActivity, NotifyJobService::class.java))
                    .addTriggerContentUri(JobInfo.TriggerContentUri(triggerUri, 0))
                    .build()
            )

            val text = "Job queued. You can now close the app and (dis)connect the watch to get the notification"
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        sender.close()
        super.onDestroy()
    }
}

@Composable
fun SampleUI(
    sendTimeToWatch: () -> Unit,
    openApp: () -> Unit,
    closeApp: () -> Unit,
    connectedWatchesForeground: () -> Unit,
    connectedWatchesBackground: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = sendTimeToWatch) {
            Text("Send Time to the watch")
        }

        Button(onClick = openApp) {
            Text("Open app on the watch")
        }

        Button(onClick = closeApp) {
            Text("Close app on the watch")
        }

        Spacer(Modifier.size(16.dp))

        Button(onClick = connectedWatchesForeground) {
            Text("Get connected watches (foreground)")
        }

        Button(onClick = connectedWatchesBackground) {
            Text("Get connected watches (background)")
        }
    }
}

private val APP_UUID = UUID.fromString("0054f75d-e60a-4932-8f8d-fe5c7dd365f6")

@Preview(showBackground = true)
@Composable
fun SampleUIPreview() {
    PebbleKitSampleTheme {
        SampleUI({}, {}, {}, {}, {})
    }
}
