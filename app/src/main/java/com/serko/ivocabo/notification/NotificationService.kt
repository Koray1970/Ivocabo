package com.serko.ivocabo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.serko.ivocabo.R
import com.serko.ivocabo.bluetooth.BleScanner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationService @Inject constructor(@ApplicationContext private val applicationContext: Context) {
    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val contentText = applicationContext.getString(R.string.ntf_summary)

    private val soundUri =
        Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${applicationContext.packageName}/raw/alarm.mp3")

    fun showNotification(notifyid: Int = 100, devicename: String, macaddress: String) {
        if (notificationManager.areNotificationsEnabled()) {
            val notifIntent = PendingIntent.getBroadcast(
                applicationContext,
                22,
                Intent(
                    applicationContext,
                    NotificationReceiver::class.java
                ).apply { putExtra("macaddress", macaddress) },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

            Log.v("BleScanner", "macaddress = $macaddress")
            val notification = NotificationCompat.Builder(applicationContext, "ivoNotification")
                .setSmallIcon(R.drawable.t3_icon_32)
                .setContentTitle(applicationContext.getString(R.string.ntf_title))
                .setContentText(String.format(contentText, devicename))
                .addAction(
                    R.drawable.baseline_clear_24,
                    macaddress,
                    //applicationContext.getString(R.string.removefromscanlist),
                    notifIntent
                )
                .setSound(soundUri)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            //(0..1000000).shuffled().last()
            notificationManager.notify(notifyid, notification)
        }
    }
}