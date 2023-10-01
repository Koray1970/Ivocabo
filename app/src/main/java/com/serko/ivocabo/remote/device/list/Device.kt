package com.serko.ivocabo.remote.device.list

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Device(
    @SerializedName("description")
    val description: String?,
    @SerializedName("ismissing")
    val ismissing: Boolean?,
    @SerializedName("istracking")
    val istracking: Boolean?,
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?,
    @SerializedName("macaddress")
    val macaddress: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("newmacaddress")
    val newmacaddress: String?,
    @SerializedName("devicetype")
    val devicetype: Int?
)