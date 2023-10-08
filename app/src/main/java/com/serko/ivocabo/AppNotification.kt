package com.serko.ivocabo

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.serko.ivocabo.data.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class NotifyItem(val device: Device, val title: String, val summary:String, val content: String)
@SuppressLint("MissingPermission")
class AppNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    val request: NotifyItem
) {
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelID = "ivoNotification"

    init {
        if (notificationManager.areNotificationsEnabled()) {
            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setTicker("")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(request.title)
                        .setSummaryText(request.device.name + " " + request.content)
                        .bigText(request.device.name + " " + request.content)
                )
                .setSmallIcon(R.drawable.baseline_warning_amber_24)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()
            with(NotificationManagerCompat.from(context)) {
                notify((0..1000000).shuffled().last(), notificationBuilder)
            }
        }
    }

}