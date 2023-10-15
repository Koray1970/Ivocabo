package com.serko.ivocabo

import android.app.NotificationManager
import android.content.Context
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

            var userToken = inputData.getString("usertoken")
            if (!userToken.isNullOrEmpty()) {

                var getlocation = AppFusedLocationRepo(_context)
                var lastLatLng: LatLng? = null
                lastLatLng = getlocation.startCurrentLocation()
                    .flowOn(Dispatchers.Default)
                    .cancellable().first()

                if (IApiService.apiService == null)
                    IApiService.getInstance()
                val apiSrv = IApiService.apiService
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
                                /*MainScope().launch {
                                    delay(100)
                                    val rdiva = bluetoothScanService.bluetoothScanner()
                                        .flowOn(Dispatchers.Default).cancellable().first()
                                    delay(200)
                                    bluetoothScanService.stopLocationJob.tryEmit(true)
                                    if (rdiva != null) {
                                        val listofRemoteRequest =
                                            AddBulkMissingDeviceTrakingRequest()
                                        rdiva.forEach { rr ->
                                            listofRemoteRequest.add(
                                                AddBulkMissingDeviceTrakingRequestItem(
                                                    rr.macaddress!!,
                                                    Trackstory(
                                                        helper.getNowAsJsonString(),
                                                        lastLatLng?.latitude.toString(),
                                                        lastLatLng?.longitude.toString()
                                                    )
                                                )
                                            )
                                        }
                                        *//*Log.v(
                                            "TrackWork",
                                            "listofRemoteRequest Size=${listofRemoteRequest.size}"
                                        )*//*
                                        SendScanListToRemote(
                                            apiSrv,
                                            userToken!!,
                                            listofRemoteRequest
                                        )
                                    }
                                }*/
                            }
                        }
                    }

                    override fun onFailure(call: Call<MissingDeviceListResponse>, t: Throwable) {

                    }
                })
            }
        } catch (e: Exception) {
        }

        return Result.success()
    }

    fun SendScanListToRemote(
        apiSrv: IApiService,
        token: String,
        listofTrack: AddBulkMissingDeviceTrakingRequest
    ) {
        /*val gson=Gson()
        Log.v("TrackWorker","token = $token")
        Log.v("TrackWorker","listofTrac = ${gson.toJson(listofTrack)}")*/
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