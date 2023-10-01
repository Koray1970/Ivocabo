package com.serko.ivocabo.remote.membership

import com.serko.ivocabo.data.Device

data class SignInResponse(
    val eventResult: EventResult,
    val token: String,
    val username:String?,
    val email:String?,
    val devicelist:ArrayList<Device>?
)