package com.serko.ivocabo.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.serko.ivocabo.pages.gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BleScanner @Inject constructor(@ApplicationContext private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter

    private val scanSettings = ScanSettings
        .Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(250)
        .build()

    @SuppressLint("MissingPermission")
    fun getScanResults(): Flow<MutableList<ScanResult>?> {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        return callbackFlow {
            val scanCallback = object : ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    trySend(results)
                    super.onBatchScanResults(results)
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.v("onScanFailed", "errorCode = $errorCode")
                    super.onScanFailed(errorCode)
                }
            }
            Log.v("MainActivity", "scanFilters size=${scanFilters.size}")
            when (scanFilters.size) {
                0 -> {
                    bluetoothLeScanner.stopScan(scanCallback)
                    SCAN_STATE.value = BleScannerScanState.STOP_SCAN
                    Log.v("MainActivity", "STOP_SCAN")
                }

                else -> {
                    when (SCAN_STATE.value) {
                        BleScannerScanState.STOP_SCAN -> {
                            bluetoothLeScanner.stopScan(scanCallback)
                            Log.v("MainActivity", "STOP_SCAN")
                        }

                        BleScannerScanState.START_SCAN -> {
                            bluetoothLeScanner.stopScan(scanCallback)
                            Log.v("MainActivity", "STOP_SCAN")
                            delay(2000)
                            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
                            Log.v("MainActivity", "START_SCAN")
                        }
                    }
                }
            }
            awaitClose {
                bluetoothLeScanner.stopScan(scanCallback)
                SCAN_STATE.value = BleScannerScanState.STOP_SCAN
                Log.v("MainActivity", "STOP_SCAN")
            }
        }
    }

    companion object {
        val SCAN_STATE: MutableState<BleScannerScanState> =
            mutableStateOf(BleScannerScanState.START_SCAN)
        val scanFilters = mutableListOf<ScanFilter>()
    }
}

enum class BleScannerScanState { START_SCAN, STOP_SCAN }