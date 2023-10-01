package com.serko.ivocabo.remote.device.list

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.serko.ivocabo.remote.membership.EventResult
@Keep
data class DeviceListResponse(
    @SerializedName("devices")
    val devices: List<Device>?,
    @SerializedName("eventResult")
    val eventResult: EventResult
)