package com.serko.ivocabo.pages

import android.bluetooth.le.ScanFilter
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.serko.ivocabo.R
import com.serko.ivocabo.bluetooth.BluetoothScanService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn

@Composable
fun ComposeScanResultUI(
    context: Context,
    macaddress: String,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    metricDistance.value = context.getString(R.string.scanning)
    var disconnectedCounter = 0
    if (bluetoothScanService == null) {
        bluetoothScanService = BluetoothScanService(context)
    }
    if (!bluetoothScanService.scanList.isNullOrEmpty()) {
        if (bluetoothScanService.scanList.none { a -> a.deviceAddress == macaddress }) {
            bluetoothScanService.scanList.add(
                ScanFilter.Builder().setDeviceAddress(macaddress).build()
            )
        }
    } else {
        bluetoothScanService.scanList.add(
            ScanFilter.Builder().setDeviceAddress(macaddress).build()
        )
    }
    bluetoothScanService!!.scanJonState.tryEmit(true)


    metricDistanceTextStyle.value = scanningMetricTextStyle
    metricDistance.value = context.getString(R.string.scanning)
    var cRssi by remember { mutableStateOf<Int?>(null) }/*val notify = AppNotification(
    context,
    NotifyItem(deviceDetail, "Title", "Summary", "Context"))*/

    LaunchedEffect(Unit) {
        bluetoothScanService.bluetoothScannerResults().flowOn(Dispatchers.Default).cancellable()
            .collect { rlt ->
                if (!rlt.isNullOrEmpty()) {
                    when (rlt.size) {
                        1 -> {
                            Log.v("MainActivity", "bluetoothScannerResults 2")
                            var dvScanResult = rlt.first { a -> a.macaddress == macaddress }
                            if (dvScanResult != null) {
                                disconnectedCounter = 0
                                metricDistanceTextStyle.value = defaultMetricTextStyle
                                if (composeProgressStatus.value) composeProgressStatus.value = false
                                cRssi = dvScanResult.rssi
                                metricDistance.value =
                                    helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                                Log.v("MainActivity", "bluetoothScannerResults 2.1")
                            } else {
                                disconnectedCounter = disconnectedCounter + 1
                                Log.v("MainActivity", "bluetoothScannerResults 2.2")
                                if (disconnectedCounter >= 10) {
                                    cRssi = null
                                    metricDistanceTextStyle.value = scanningMetricTextStyle
                                    metricDistance.value =
                                        context.getString(R.string.devicecannotbereached)
                                    Log.v("MainActivity", "bluetoothScannerResults 2.3")
                                } else metricDistance.value =
                                    helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                            }

                        }

                        else -> {

                            if (cRssi != null) metricDistance.value =
                                helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                            else metricDistance.value = context.getString(R.string.scanning)
                            disconnectedCounter = disconnectedCounter + 1
                            Log.v("MainActivity", "bluetoothScannerResults 3")
                            if (disconnectedCounter >= 10) {
                                cRssi = null
                                metricDistanceTextStyle.value = scanningMetricTextStyle
                                metricDistance.value =
                                    context.getString(R.string.devicecannotbereached)
                                Log.v("MainActivity", "bluetoothScannerResults 3.1")
                            }
                        }
                    }
                } else {
                    Log.v("MainActivity", "bluetoothScannerResults 1")
                    if (cRssi != null) metricDistance.value =
                        helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                    else metricDistance.value = context.getString(R.string.scanning)
                    disconnectedCounter = disconnectedCounter + 1
                    if (disconnectedCounter >= 10) {
                        cRssi = null
                        metricDistanceTextStyle.value = scanningMetricTextStyle
                        metricDistance.value = context.getString(R.string.devicecannotbereached)
                        Log.v("MainActivity", "bluetoothScannerResults 1.1")
                    }
                }
            }
    }
}