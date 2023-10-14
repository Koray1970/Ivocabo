package com.serko.ivocabo

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@HiltWorker
class TrackWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters
) :
    CoroutineWorker(context, parameters) {
    private val gson = Gson()
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var bluetoothScanService: BluetoothScanService
    private val channelID = "ivoNotification"
    private val _context = context
    private var device: Device? = null
    private val helper = Helper()
    override suspend fun doWork(): Result {
        //get remote missing device list
        try {

            var bluetoothScanService = BluetoothScanService(_context)

            if (IApiService.apiService == null)
                IApiService.getInstance()
            val apiSrv = IApiService.apiService

            var userToken = inputData.getString("usertoken")

            //var missingDeviceList = flowOf<MutableList<String>>()
            //get missing device list from remote
            val call: Call<MissingDeviceListResponse> =
                apiSrv?.srvMissingDeviceList("Bearer $userToken")!!
            call.enqueue(object : Callback<MissingDeviceListResponse> {
                override fun onResponse(
                    call: Call<MissingDeviceListResponse>,
                    response: Response<MissingDeviceListResponse>
                ) {
                    if (response.isSuccessful) {
                        val rBody = response.body()!!
                        if (rBody.eventResult.eventresultflag == EventResultFlags.SUCCESS.flag) {
                            var locdiva = mutableListOf<String>()
                            rBody.devicelist.forEach { diva ->
                                locdiva.add(diva.macaddress)
                            }

                            bluetoothScanService.flowListOfMacaddress = flowOf(locdiva)
                        }
                    }
                }

                override fun onFailure(call: Call<MissingDeviceListResponse>, t: Throwable) {

                }

            })

            bluetoothScanService.flowListOfMacaddress.flowOn(Dispatchers.Default).collect { diva ->
                if (diva.isNotEmpty()) {
                    bluetoothScanService.bluetoothScanner().flowOn(Dispatchers.Default)
                        .collect { rdiva ->
                            if (rdiva?.isNotEmpty() == true) {
                                var getlocation = AppFusedLocationRepo(_context)
                                var lastLatLng: LatLng? = null
                                getlocation.startCurrentLocation().flowOn(Dispatchers.Default)
                                    .collect { loc ->
                                        if (loc != null) {
                                            lastLatLng = loc

                                            Log.v("TrackWork","Loc=${lastLatLng!!.latitude} - ${lastLatLng!!.longitude}")
                                            delay(100)
                                            getlocation.stopLocationJob.tryEmit(true)
                                            val listofRemoteRequest =
                                                AddBulkMissingDeviceTrakingRequest()
                                            rdiva.forEach { rr ->
                                                listofRemoteRequest.add(
                                                    AddBulkMissingDeviceTrakingRequestItem(
                                                        rr.macaddress!!,
                                                        Trackstory(
                                                            helper.getNOWasString(),
                                                            loc.latitude.toString(),
                                                            loc.longitude.toString()
                                                        )
                                                    )
                                                )
                                            }
                                            SendScanListToRemote(
                                                apiSrv,
                                                userToken!!,
                                                listofRemoteRequest
                                            )
                                        }
                                        delay(100)
                                        bluetoothScanService.stopLocationJob.tryEmit(true)
                                    }

                            }
                            delay(100)
                            bluetoothScanService.stopLocationJob.tryEmit(true)
                        }
                } else {
                    bluetoothScanService.stopLocationJob.tryEmit(true)
                }
            }
        }
        catch (e:Exception){}

        return Result.success()
    }

    fun SendScanListToRemote(
        apiSrv: IApiService,
        token: String,
        listofTrack: AddBulkMissingDeviceTrakingRequest
    ) {
        val call: Call<Void> =
            apiSrv?.srvAddBulkMissingDeviceTracking("Bearer $token", listofTrack)!!
        call.enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.isSuccessful) {
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

}