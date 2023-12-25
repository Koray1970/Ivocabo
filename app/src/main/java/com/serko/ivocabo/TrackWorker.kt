package com.serko.ivocabo

import android.app.NotificationManager
import android.bluetooth.le.ScanFilter
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
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
    private lateinit var soundUri: Uri
    private val TRACKING_COUNTER = 5
    var DISCONNECTED_COUNTER = 0

    override suspend fun doWork(): Result {
        //get remote missing device list
        try {
            soundUri =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + _context.packageName + "/" + R.raw.alarm)
            var dataMacaddress = inputData.getString("macaddress")
            if (!dataMacaddress.isNullOrEmpty()) {
                dataMacaddress = dataMacaddress.uppercase(Locale.ROOT)
                setForeground(
                    createForegroundInfo(
                        String.format(
                            applicationContext.getString(R.string.ntf_scanning),
                            dataMacaddress
                        ), null
                    )
                )

                /*val bluetoothScanService = BluetoothScanService(_context)

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

                val disconnectedContent = String.format(
                    applicationContext.getString(R.string.ntf_summary),
                    dataMacaddress
                )
                //val stillconnectedContent=String.format(applicationContext.getString(R.string.ntf_bigtextstillconnected),dataMacaddress)
                bluetoothScanService.bluetoothScannerResults().flowOn(Dispatchers.Default)
                    .cancellable()
                    .collect { rlt ->
                        if (!rlt.isNullOrEmpty()) {
                            when (rlt.size) {
                                1 -> {
                                    if (rlt.none { a -> a.macaddress == dataMacaddress }) {
                                        DISCONNECTED_COUNTER += 1
                                        if (DISCONNECTED_COUNTER >= TRACKING_COUNTER) {
                                            setForeground(
                                                createForegroundInfo(
                                                    disconnectedContent,
                                                    soundUri
                                                )
                                            )
                                        }
                                    } else {
                                        DISCONNECTED_COUNTER = 0
                                    }
                                }

                                else -> {
                                    DISCONNECTED_COUNTER += 1
                                    if (DISCONNECTED_COUNTER >= TRACKING_COUNTER) {
                                        //device can not be reached
                                        setForeground(
                                            createForegroundInfo(
                                                disconnectedContent,
                                                soundUri
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            DISCONNECTED_COUNTER += 1
                            if (DISCONNECTED_COUNTER >= TRACKING_COUNTER) {
                                //device can not be reached
                                setForeground(createForegroundInfo(disconnectedContent, soundUri))
                            }
                        }
                    }*/
            }
        } catch (e: Exception) {
        }

        return Result.success()
    }

    private fun createForegroundInfo(content: String, soundUri: Uri?): ForegroundInfo {
        val id = "ivoNotification"
        val notificationId = Math.random().roundToInt()
        val title = applicationContext.getString(R.string.ntf_title)
        val cancel = applicationContext.getString(R.string.cancel)
        var content = content
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_track_changes_24)
            //.setOngoing(true)
            //.setSound(soundUri)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
        //.build()
        if (DISCONNECTED_COUNTER >= TRACKING_COUNTER)
            if (soundUri != null)
                notification.setSound(soundUri)
        return ForegroundInfo(notificationId, notification.build())
    }
}