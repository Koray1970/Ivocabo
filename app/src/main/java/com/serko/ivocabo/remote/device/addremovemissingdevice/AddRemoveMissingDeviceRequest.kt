package com.serko.ivocabo.remote.device.addremovemissingdevice

data class AddRemoveMissingDeviceRequest(
    val macaddress: String,
    val status: Int
)