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
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.serko.ivocabo.bluetooth.BluetoothScanner
import com.serko.ivocabo.bluetooth.BluetoothScannerCallbackStatus
import com.serko.ivocabo.bluetooth.BluetoothScannerResult
import com.serko.ivocabo.data.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
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

        inputData.getString("device")?.let { it ->
            device = gson.fromJson(it, Device::class.java)
            val macaddress = device!!.macaddress.uppercase(Locale.ROOT)
            val listofMacaddress = mutableListOf<String>()
            listofMacaddress.add(macaddress)
            IS_SCANNING.postValue(true)
            bluetoothScanner = BluetoothScanner(_context, listofMacaddress)



            when (SCANNING_STATUS.value) {
                true -> {
                    delay(600)
                    bluetoothScanner?.StartScan()
                    delay(5000)
                    IS_SCANNING.postValue(true)
                    bluetoothScanner!!.getBluetoothScannerResults().observeForever { rslt ->
                        if (rslt.isNotEmpty()) {
                            var getCurrentDeviceResult =
                                rslt!!.last { a -> a.macaddress == macaddress }
                            if (getCurrentDeviceResult.callbackStatus == BluetoothScannerCallbackStatus.CONNECTION_LOST) {
                                bluetoothScanner?.StopScan()
                                IS_SCANNING.postValue(false)
                                IS_DEVICE_LOST.postValue(Pair(true, getCurrentDeviceResult))
                            }
                        }
                    }
                }

                else -> {
                    bluetoothScanner?.StopScan()
                    IS_SCANNING.postValue(false)

                }
            }
            when (IS_DEVICE_LOST.value?.first) {
                true -> {
                    setForeground(
                        createForegroundInfo(
                            IS_DEVICE_LOST.value?.second,
                            macaddress
                        )
                    )
                }
                null->Nothing()
                else -> Nothing()
            }
        }
        return Result.success()
    }
    fun  Nothing(){

    }
    @SuppressLint("InlinedApi")
    private fun createForegroundInfo(
        scanResult: BluetoothScannerResult?,
        cmacaddress: String
    ): ForegroundInfo {

        val notificationID = (0..1000000).shuffled().last()
        val intent = WorkManager.getInstance(_context)
            .createCancelPendingIntent(UUID.randomUUID())


        val notificationBuilder = NotificationCompat.Builder(_context, channelID)
            .setTicker("")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(_context.getString(R.string.ntf_title))
                    .setSummaryText(
                        String.format(
                            _context.getString(R.string.ntf_title),
                            scanResult?.macaddress ?: cmacaddress
                        )
                    )
                    .bigText(
                        String.format(
                            _context.getString(R.string.ntf_title),
                            scanResult?.macaddress ?: cmacaddress
                        )
                    )
            )
            .setSmallIcon(R.drawable.baseline_warning_amber_24)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
        return ForegroundInfo(notificationID, notificationBuilder)
    }

    companion object {
        private var IS_DEVICE_LOST = MutableLiveData(
            Pair<Boolean, BluetoothScannerResult>(
                false,
                BluetoothScannerResult(null, null, null, null)
            )
        )

        /*var CURRENT_RSSI = mutableStateOf<Int?>(null)*/
        var IS_SCANNING = MutableLiveData<Boolean>(true)

        //true scanning false stop scanning
        var SCANNING_STATUS = mutableStateOf(false)
    }
}