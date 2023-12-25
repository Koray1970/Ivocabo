package com.serko.ivocabo.pages

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.serko.ivocabo.Helper
import com.serko.ivocabo.bluetooth.BluetoothScanService
import com.serko.ivocabo.data.Device

val gson = Gson()
val helper = Helper()
val dummyDevice = Device(null, "", null, "", null, null, null, null, null, null)
lateinit var bluetoothScanService: BluetoothScanService

val formTitle =
    TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
val defaultMetricTextStyle = TextStyle(
    fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, textAlign = TextAlign.Center
)
val scanningMetricTextStyle = TextStyle(
    fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.Red
)
var metricDistance = mutableStateOf("")
var metricDistanceTextStyle = mutableStateOf<TextStyle>(defaultMetricTextStyle)
fun DoNothing() {}