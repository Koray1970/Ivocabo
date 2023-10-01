package com.serko.ivocabo.api

import com.serko.ivocabo.remote.device.addmissingdevicetracking.AddMissingDeviceTrackingRequest
import com.serko.ivocabo.remote.device.addremovemissingdevice.AddRemoveMissingDeviceRequest
import com.serko.ivocabo.remote.device.addupdate.DeviceAddUpdateRequest
import com.serko.ivocabo.remote.device.list.DeviceListResponse
import com.serko.ivocabo.remote.device.missingdevicelist.MissingDeviceListResponse
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.SignInRequest
import com.serko.ivocabo.remote.membership.SignInResponse
import com.serko.ivocabo.remote.membership.SignUpRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

const val BASE_URL = "https://api.ivocabo.com/"

interface IApiService {
    @POST("membership/registeruser")
    fun srvSignUp(@Body request: SignUpRequest): Call<EventResult>

    //@Headers("Accept: application/json")
    @POST("membership/signin")
    fun srvSignIn(@Body request: SignInRequest): Call<SignInResponse>

    @POST("device/devicelist")
    fun srvDeviceList(@Header("authorization") token: String): Call<DeviceListResponse>

    @POST("device/addupdateDevice")
    fun srvAddUpdateDevice(@Header("authorization") token: String,@Body request: DeviceAddUpdateRequest): Call<EventResult>

    @POST("device/deviceremove")
    fun srvDeviceRemove(@Header("authorization") token: String, @Query("macaddress") macaddress: String): Call<EventResult>

    @POST("device/missingdevicelist")
    fun srvMissingDeviceList(@Header("authorization") token: String): Call<MissingDeviceListResponse>

    @POST("device/addremovemissingdevice")
    fun srvAddRemoveMissingDevice(@Header("authorization") token: String,@Body request: AddRemoveMissingDeviceRequest): Call<EventResult>

    @POST("device/addmissingdevicetracking")
    fun srvAddMissingDeviceTracking(@Header("authorization") token: String,@Body request: AddMissingDeviceTrackingRequest): Call<EventResult>

    companion object {
        var apiService: IApiService? = null
        fun getInstance(): IApiService {
            if (apiService == null) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

                apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build().create(IApiService::class.java)
            }
            return apiService!!
        }
    }
}