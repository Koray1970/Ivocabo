package com.serko.ivocabo

import android.Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.serko.ivocabo.bluetooth.BluetoothScanner
import com.serko.ivocabo.data.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

class TrackWorker @Inject constructor(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val gson = Gson()
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelID = "ivoNotification"
    private val _context = context
    private var device: Device? = null

    private var bluetoothScanner: BluetoothScanner? = null
    override suspend fun doWork(): Result {

        bluetoothScanner = BluetoothScanner(_context)
        inputData.getString("device")?.let { it ->
            device = gson.fromJson(it, Device::class.java)
            bluetoothScanner?.listOfMacaddress?.add(device!!.macaddress.uppercase(Locale.ROOT))

            Log.v("TrackWorker", "Mac  : ${device!!.macaddress.uppercase(Locale.ROOT)}")
            setForeground(createForegroundInfo(device!!))
            MainScope().launch {
                when (SCANNING_STATUS.value) {
                    true -> {
                        delay(600)
                        bluetoothScanner?.StartScan()
                        IS_SCANNING.value = false
                        delay(5000)
                        bluetoothScanner!!.getCurrentRSSI().observeForever { rssi ->
                            when (rssi) {
                                null -> {
                                    createForegroundInfo(device!!)
                                    Log.v("TrackWorker", "RSSI : null")
                                }

                                else -> {
                                    CURRENT_RSSI.value = rssi
                                    Log.v("TrackWorker", "RSSI : $rssi")
                                }
                            }
                        }
                        /*bluetoothScanner?.getFlowCurrentRSSI()?.collect { rssi ->
                            when (rssi) {
                                null -> {
                                    createForegroundInfo(device!!)
                                    Log.v("TrackWorker","RSSI : null")
                                }
                                else -> {
                                    CURRENT_RSSI.value = rssi
                                    Log.v("TrackWorker","RSSI : $rssi")
                                }
                            }
                        }*/
                    }

                    else -> {
                        bluetoothScanner?.StopScan()
                    }
                }
            }
        }
        return Result.success()
    }


    @SuppressLint("InlinedApi")
    private fun createForegroundInfo(device: Device): ForegroundInfo {
        val notificationBuilder = NotificationCompat.Builder(_context, channelID)
            .setTicker("")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(_context.getString(R.string.ntf_title))
                    .setSummaryText(
                        String.format(
                            _context.getString(R.string.ntf_title),
                            device.macaddress
                        )
                    )
                    .bigText(String.format(_context.getString(R.string.ntf_title), device.name))
            )
            .setSmallIcon(R.drawable.baseline_warning_amber_24)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
        return ForegroundInfo((0..1000000).shuffled().last(), notificationBuilder)
    }

    companion object {
        var CURRENT_RSSI = mutableStateOf<Int?>(null)
        var IS_SCANNING = mutableStateOf(false)

        //true scanning false stop scanning
        var SCANNING_STATUS = mutableStateOf(false)
    }
}