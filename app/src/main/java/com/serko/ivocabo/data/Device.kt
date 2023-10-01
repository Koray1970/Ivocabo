package com.serko.ivocabo.data

import java.util.Date


data class Device(
    var registerdate: String?,
    var name: String,
    var devicetype:Int?,
    var macaddress: String,
    var description: String?,
    var latitude: String?,
    var longitude: String?,
    var istracking: Boolean?,
    var ismissing: Boolean?,
    var newmacaddress:String?
)

