package com.serko.ivocabo.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BluetoothScanStates {
    INIT, START_SCANNING, SCANNING, STOP_SCANNING
}

class BleScanner(@ApplicationContext private val applicationContext: Context) {
    private val TAG = BleScanner::class.java.name
    private val gson = Gson()
    private val bluetoothManager =
        applicationContext.getSystemService(BluetoothManager::class.java) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val REPORT_DELAY = 2200L
    private var scanFilter: MutableList<ScanFilter>? = null

    init {
        MainScope().launch {
            Log.v(TAG, scanStatus.value.name)
            while (true) {
                when (scanDeviceMacaddres.isEmpty()) {
                    true -> {
                        scanFilter = null
                        delay(200)
                        stopScanning()
                        scanStatus.value = BluetoothScanStates.STOP_SCANNING
                    }

                    false -> {
                        Log.v(TAG, gson.toJson(scanDeviceMacaddres))
                        Log.v(TAG, scanStatus.value.name)
                        if (scanFilter == null)
                            scanFilter = mutableListOf<ScanFilter>()
                        scanDeviceMacaddres.forEach { s ->
                            if (scanFilter!!.none { a -> a.deviceAddress == s }) {
                                scanFilter!!.add(
                                    ScanFilter.Builder().setDeviceAddress(s).build()
                                )
                            }
                        }
                        delay(320)
                        when (scanStatus.value) {
                            BluetoothScanStates.START_SCANNING -> {
                                startScaning()
                                scanStatus.value = BluetoothScanStates.SCANNING
                            }

                            else -> {
                                if (scanStatus.value == BluetoothScanStates.STOP_SCANNING) {
                                    scanFilter = null
                                    delay(200)
                                    stopScanning()
                                }
                            }
                        }
                    }
                }
                delay(2000)
            }
        }
    }


    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(REPORT_DELAY)
        .build()
    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.v(TAG, gson.toJson(results))
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.v(TAG, gson.toJson(errorCode))
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScaning() {
        if (scanFilter != null) {

            bluetoothLeScanner.startScan(scanFilter, scanSettings, scanCallback)
        } else {
            stopScanning()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    companion object {
        var scanStatus = mutableStateOf(BluetoothScanStates.INIT)
        var scanDeviceMacaddres = mutableListOf<String>()
    }
}