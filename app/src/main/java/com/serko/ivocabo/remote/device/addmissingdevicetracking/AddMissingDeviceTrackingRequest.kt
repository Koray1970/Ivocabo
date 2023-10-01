package com.serko.ivocabo.remote.device.addmissingdevicetracking

data class AddMissingDeviceTrackingRequest(
    val macaddress: String,
    val trackstory: Trackstory
)