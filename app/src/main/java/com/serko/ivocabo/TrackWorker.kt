package com.serko.ivocabo

import android.app.NotificationManager
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.bluetooth.BluetoothScanService
import com.serko.ivocabo.data.Device
import com.serko.ivocabo.location.AppFusedLocationRepo
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.AddBulkMissingDeviceTrakingRequest
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.AddBulkMissingDeviceTrakingRequestItem
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.Trackstory
import com.serko.ivocabo.remote.device.missingdevicelist.MissingDeviceListResponse
import com.serko.ivocabo.remote.membership.EventResultFlags
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.math.roundToInt

@HiltWorker
class TrackWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters
) :
    CoroutineWorker(context, parameters) {
    val _context = context
    override suspend fun doWork(): Result {
        //get remote missing device list
        try {
            var dataMacaddress = inputData.getString("macaddress")
            if (!dataMacaddress.isNullOrEmpty()) {
                dataMacaddress = dataMacaddress.uppercase(Locale.ROOT)
                setForeground(createForegroundInfo(String.format(applicationContext.getString(R.string.ntf_scanning),dataMacaddress)))

                val bluetoothScanService = BluetoothScanService(_context)

                //start::set scan list
                if (bluetoothScanService.scanList.isNotEmpty()) {
                    if (bluetoothScanService.scanList.none { a -> a.deviceAddress == dataMacaddress }) {
                        bluetoothScanService.scanList.add(
                            ScanFilter.Builder().setDeviceAddress(dataMacaddress).build()
                        )
                    }
                } else {
                    bluetoothScanService.scanList.add(
                        ScanFilter.Builder().setDeviceAddress(dataMacaddress).build()
                    )
                }
                //end::set scan list
                delay(100)
                if (!bluetoothScanService.scanJonState.value)
                    bluetoothScanService.scanJonState.tryEmit(true)

                delay(200)
                var disconnectedCounter=0
                val disconnectedContent=String.format(applicationContext.getString(R.string.ntf_summary),dataMacaddress)
                val stillconnectedContent=String.format(applicationContext.getString(R.string.ntf_bigtextstillconnected),dataMacaddress)
                bluetoothScanService.bluetoothScannerResults().flowOn(Dispatchers.Default).cancellable()
                    .collect{rlt->
                        if (!rlt.isNullOrEmpty()) {
                            when (rlt.size) {
                                1 -> {
                                    var dvScanResult = rlt.first { a -> a.macaddress == dataMacaddress }
                                    if (dvScanResult == null) {
                                        disconnectedCounter = disconnectedCounter + 1
                                        if (disconnectedCounter >= 10) {
                                            setForeground(createForegroundInfo(disconnectedContent))

                                            //device can not be reached
                                        }
                                    }
                                    else {
                                        disconnectedCounter = 0
                                        setForeground(createForegroundInfo(stillconnectedContent))
                                    }
                                }

                                else -> {
                                    disconnectedCounter = disconnectedCounter + 1
                                    if (disconnectedCounter >= 10) {
                                        //device can not be reached
                                        setForeground(createForegroundInfo(disconnectedContent))
                                    }
                                }
                            }
                        } else {
                            disconnectedCounter = disconnectedCounter + 1
                            if (disconnectedCounter >= 10) {
                                //device can not be reached
                                setForeground(createForegroundInfo(disconnectedContent))
                            }
                        }
                    }
            }
        } catch (e: Exception) {
        }

        return Result.success()
    }

    private fun createForegroundInfo(content: String): ForegroundInfo {
        val id = "ivoNotification"
        val notificationId=Math.random().roundToInt()
        val title = applicationContext.getString(R.string.ntf_title)
        val cancel = applicationContext.getString(R.string.cancel)
        var content=content
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_track_changes_24)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(notificationId, notification)
    }
}