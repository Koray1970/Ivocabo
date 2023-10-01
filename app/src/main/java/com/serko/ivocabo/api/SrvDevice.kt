package com.serko.ivocabo.api

import com.google.gson.Gson
import com.serko.ivocabo.remote.device.list.DeviceListResponse
import com.serko.ivocabo.remote.membership.Error
import com.serko.ivocabo.remote.membership.EventResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SrvDevice {
    val gson: Gson = Gson()
    fun invokeDeviceListService(token: String): DeviceListResponse {
        var result = DeviceListResponse(
            eventResult = EventResult(Error("", "", ""), eventresultflag = 1),
            devices = null
        )
        try {
            IApiService.getInstance()
            val apiSrv = IApiService.apiService

            val call: Call<DeviceListResponse>? = apiSrv?.srvDeviceList(token = token)
            call!!.enqueue(object : Callback<DeviceListResponse> {
                override fun onResponse(
                    call: Call<DeviceListResponse>,
                    response: Response<DeviceListResponse>,
                ) {
                    result = if (response.isSuccessful) {
                        response.body()!!
                    } else {
                        gson.fromJson(
                            response.errorBody()!!.charStream(),
                            DeviceListResponse::class.java
                        )
                    }
                }

                override fun onFailure(call: Call<DeviceListResponse>, t: Throwable) {
                    result.eventResult.error = Error(
                        "SRV_SDVL10",
                        t.message.toString(),
                        "com.serko.ivocabo.api.SrvDevice.invokeDeviceListService.call.enqueue.onFailure"
                    )
                }
            })
        } catch (e: Exception) {
            result.eventResult.error = Error(
                "SRV_SDVL01",
                e.message.toString(),
                "com.serko.ivocabo.api.SrvDevice.invokeDeviceListService"
            )
        }
        return result
    }
}