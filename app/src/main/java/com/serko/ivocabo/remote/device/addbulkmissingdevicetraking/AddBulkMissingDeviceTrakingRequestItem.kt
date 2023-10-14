package com.serko.ivocabo.remote.device.addbulkmissingdevicetraking

data class AddBulkMissingDeviceTrakingRequestItem(
    val macaddress: String,
    val trackstory: Trackstory
)