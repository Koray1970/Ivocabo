package com.serko.ivocabo.remote.device.addupdate

data class DeviceAddUpdateRequest(
    val description: String?,
    val devicetype: Int?,
    val ismissing: Boolean?,
    val istracking: Boolean?,
    val latitude: String?,
    val longitude: String?,
    val macaddress: String,
    val name: String,
    val newmacaddress: String?
)