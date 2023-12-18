package com.serko.ivocabo.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BluetoothActivityState(value: Int) {
    INIT(value = 0), SCANNING(value = 1), STOPPED(value = 2), DEVICE_LISTITEM_CHANGE(value = 3)
}

enum class ScanningDeviceFindStatus(value: Int) { FIND(value = 1), NOT_FOUND(value = 0) }
data class ScanningDeviceItem(
    val macaddress: String,
    var disconnectednum: Int? = null,
    val findStatus: ScanningDeviceFindStatus = ScanningDeviceFindStatus.NOT_FOUND,
    val rssi: Int? = null
)

@SuppressLint("MissingPermission")
@OptIn(DelicateCoroutinesApi::class)
class BluetoothActivity @Inject constructor(@ApplicationContext private val context: Context) {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanCallback: ScanCallback
    private var scanList = mutableListOf<ScanFilter>()
    private val gson = Gson()
    private lateinit var bluetoothStatusObserver: BluetoothStatusObserver


    private val scanSetting = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(DELAY_PERIOD)
        .build()

    init {
        if (bluetoothAdapter.isEnabled) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            scanCallback = object : ScanCallback() {
                @SuppressLint("MissingPermission")
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    Log.v("ScanResult = ", gson.toJson(results))
                    //Log.v("ScanResult MacList 2", gson.toJson(scanList))
                    super.onBatchScanResults(results)
                    results ?: return
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    if (errorCode == 1)
                        StopScanning()
                    Log.v("ScanResult onScanFailed", "${errorCode} ")
                }
            }
            MainScope().launch {
                while (true) {
                    if (scanningDeviceList.isNotEmpty()) {
                        //StopScanning()
                        //delay(2000)
                        scanList.removeIf { a -> scanningDeviceList.none { g -> g.macaddress == a.deviceAddress } }
                        scanningDeviceList.forEach { g ->
                            if (scanList.none { a -> a.deviceAddress == g.macaddress }) {
                                scanList.add(
                                    ScanFilter.Builder().setDeviceAddress(g.macaddress).build()
                                )
                            }
                        }
                        StopScanning()
                        delay(DELAY_PERIOD)
                        if (activityState.value != BluetoothActivityState.SCANNING)
                            StartScanning()
                        Log.v("ScanResult MacList 2", gson.toJson(scanList))
                        Log.v("ScanResult activityState.value", activityState.value.name)
                    } else {
                        scanList = mutableListOf()
                        StopScanning()
                    }
                    delay(DELAY_PERIOD)
                }
            }
        } else {
            StopScanning()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        }
    }

    private fun StartScanning() {
        bluetoothLeScanner.startScan(scanList, scanSetting, scanCallback)
        activityState.value = BluetoothActivityState.SCANNING
    }

    fun StopScanning() {
        if (activityState.value != BluetoothActivityState.STOPPED) {
            bluetoothLeScanner.stopScan(scanCallback)
            activityState.value = BluetoothActivityState.STOPPED
        }
    }

    companion object {
        val DELAY_PERIOD = 6000L
        var activityState: MutableState<BluetoothActivityState> =
            mutableStateOf(BluetoothActivityState.INIT)
        var scanningDeviceList: MutableList<ScanningDeviceItem> = mutableStateListOf()
    }
}