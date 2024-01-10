package com.serko.ivocabo

import android.content.Context
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.bluetooth.BleScanFilterItem
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.data.AppDatabase
import com.serko.ivocabo.data.FormActionError
import com.serko.ivocabo.data.FormActionResult
import com.serko.ivocabo.data.FormActionResultFlag
import com.serko.ivocabo.data.UserViewModel
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
import javax.inject.Inject

@HiltWorker
class MissingDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters
) :
    CoroutineWorker(context, parameters) {
    val appcontext = context.applicationContext
    override suspend fun doWork(): Result {

        //remove from database
        Thread {
            val database = AppDatabase.getInstance(appcontext!!).userDao()
            val getuser = database.fetchUser()

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
                    //Log.v("MainActivity","remote Response : ${response.raw().code}")
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
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
                                            delay(1000L)
                                            while (true) {
                                                if (BleScanner.scanResults.isNotEmpty()) {


                                                    cont += 1
                                                }
                                                if (cont > 20) {
                                                    break
                                                }
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
                    Log.v("MainActivity", "t : ${t.message}")
                }
            })
        }.start()

        return Result.success()
    }
}