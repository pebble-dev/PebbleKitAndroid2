package io.rebble.pebblekit2.sample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import androidx.core.app.NotificationCompat

class NotifyJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                "1",
                "Demo Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        notificationManager.notify(
            2, NotificationCompat.Builder(this, "1")
                .setContentTitle("PebbleKitDemo")
                .setContentText("A connected watch state has changed")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        )
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}
