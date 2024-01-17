package com.serko.ivocabo

import android.content.Context
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.bluetooth.BleScanFilterItem
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.data.AppDatabase
import com.serko.ivocabo.data.FormActionError
import com.serko.ivocabo.data.FormActionResult
import com.serko.ivocabo.data.FormActionResultFlag
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.location.AppFusedLocationRepo
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.AddBulkMissingDeviceTrakingRequest
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.AddBulkMissingDeviceTrakingRequestItem
import com.serko.ivocabo.remote.device.addbulkmissingdevicetraking.Trackstory
import com.serko.ivocabo.remote.device.missingdevicelist.MissingDeviceListResponse
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.EventResultFlags
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject

@HiltWorker
class MissingDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters
) :
    CoroutineWorker(context, parameters) {
    val appcontext = context.applicationContext
    val gson = Gson()
    val helper = Helper()
    override suspend fun doWork(): Result {

        //remove from database
        Thread {
            val database = AppDatabase.getInstance(appcontext!!).userDao()
            val getuser = database.fetchUser()

            val appLocation = AppFusedLocationRepo(appcontext)


            if (IApiService.apiService == null)
                IApiService.getInstance()
            val apiSrv = IApiService.apiService

            val call: Call<MissingDeviceListResponse> =
                apiSrv?.srvMissingDeviceList("Bearer ${getuser.token} ")!!
            call.enqueue(object : Callback<MissingDeviceListResponse> {
                override fun onResponse(
                    call: Call<MissingDeviceListResponse>,
                    response: Response<MissingDeviceListResponse>
                ) {
                    Log.v("MissingDeviceWorker", "Missing Device Work Running")
                    Log.v("MissingDeviceWorker", "response : ${gson.toJson(response.body())}")
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.v("MainActivity", gson.toJson(body))
                            if (body.eventResult.eventresultflag == EventResultFlags.SUCCESS.flag) {
                                if (body.devicelist.isNotEmpty()) {
                                    if (BleScanner.scanFilter.isNotEmpty()) {
                                        BleScanner(appcontext).StopScanning()
                                        body.devicelist.onEach { a ->
                                            BleScanner.scanFilter.add(
                                                BleScanFilterItem(
                                                    name = "",
                                                    macaddress = a.macaddress,
                                                    onlytrackmydeviceevent = true
                                                )
                                            )
                                        }
                                        BleScanner(appcontext).StartScanning()
                                        //wait scanning results
                                        var cont = 0
                                        MainScope().launch {
                                            var curLoc = LatLng(0.0, 0.0)
                                            appLocation.startCurrentLocation().collect {
                                                curLoc = it
                                            }
                                            delay(2400L)
                                            while (true) {
                                                Log.v(
                                                    "MissingDeviceWorker",
                                                    "scanResults : ${gson.toJson(BleScanner.scanResults)}"
                                                )
                                                if (BleScanner.scanResults.isNotEmpty()) {
                                                    val filtredScanList =
                                                        BleScanner.scanResults.filter { a -> body.devicelist.any { h -> h.macaddress.uppercase() == a.macaddress.uppercase() } }
                                                    if (filtredScanList.isNotEmpty()) {
                                                        val req =
                                                            AddBulkMissingDeviceTrakingRequest()
                                                        filtredScanList.onEach { d ->
                                                            if (curLoc.longitude != .0)
                                                                req.add(
                                                                    AddBulkMissingDeviceTrakingRequestItem(
                                                                        macaddress = d.macaddress,
                                                                        trackstory = Trackstory(
                                                                            datetime = helper.getNowAsJsonString(),
                                                                            longitude = curLoc.longitude.toString(),
                                                                            latitude = curLoc.latitude.toString()
                                                                        )
                                                                    )
                                                                )
                                                        }
                                                        val call4Missing: Call<Void> =
                                                            apiSrv.srvAddBulkMissingDeviceTracking(
                                                                "Bearer ${getuser.token}",
                                                                req
                                                            )
                                                        call4Missing.execute()
                                                    }
                                                }
                                                if (cont > 20) {
                                                    break
                                                }
                                                cont += 1
                                                delay(2400L)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<MissingDeviceListResponse>, t: Throwable) {
                    Log.v("MissingDeviceWorker", "t : ${t.message}")
                }
            })
        }.start()

        return Result.success()
    }
}