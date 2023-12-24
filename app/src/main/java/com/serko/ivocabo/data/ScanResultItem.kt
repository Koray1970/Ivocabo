package com.serko.ivocabo.data

import com.serko.ivocabo.R

data class ScanResultItem(
    val macaddress: String,
    val devicename: String = "",
    var rssi: Int? = null,
    var metricvalue: String = "${R.string.scanning}",
    val deviceicon: Int = R.drawable.t3_icon_32,
    var disconnectedcounter: Int? = null
)
