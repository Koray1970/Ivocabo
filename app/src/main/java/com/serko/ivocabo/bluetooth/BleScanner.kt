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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.serko.ivocabo.data.BleScanViewModel
import com.serko.ivocabo.pages.gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BleScanner @Inject constructor(@ApplicationContext private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val gson = Gson()

    private val scanSettings = ScanSettings
        .Builder()
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(100)
        .build()

    @SuppressLint("MissingPermission")
    fun getScanResults(): Flow<MutableList<ScanResult>?> {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        return callbackFlow {

            val scanCallback = object : ScanCallback() {
                /*override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    Log.v("MainActivity ScanFilter ", "scanFilters = ${gson.toJson(scanFilters)}")
                    Log.v("MainActivity ScanResult", gson.toJson(result))
                    //trySend(result)
                    super.onScanResult(callbackType, result)
                }*/

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    trySend(results).isSuccess
                    super.onBatchScanResults(results)
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.v("onScanFailed", "errorCode = $errorCode")
                    super.onScanFailed(errorCode)
                }
            }
            bluetoothLeScanner.flushPendingScanResults(scanCallback)
            Log.v("MainActivity", "SCAN_STATE= ${SCAN_STATE.value}")
            Log.v("MainActivity ScanFilter33 ", "scanFilters = ${gson.toJson(scanFilters)}")
            when (scanFilters.isNullOrEmpty()) {
                true -> {
                    bluetoothLeScanner.stopScan(scanCallback)
                    scanFilters = null
                    delay(3000)
                    bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                    SCAN_STATE.value = BleScannerScanState.STOP_SCAN
                    Log.v("MainActivity", "STOP_SCAN 1")
                }

                false -> {
                    when (SCAN_STATE.value) {
                        BleScannerScanState.STOP_SCAN -> {
                            bluetoothLeScanner.stopScan(scanCallback)
                            scanFilters = null
                            delay(3000)
                            Log.v("MainActivity", "STOP_SCAN 2")
                        }

                        BleScannerScanState.START_SCAN -> {
                            //bluetoothLeScanner.stopScan(scanCallback)
                            //Log.v("MainActivity", "STOP_SCAN 3")
                            //delay(3000)
                            /*Log.v(
                                "MainActivity ScanFilter22 ",
                                "scanFilters = ${gson.toJson(scanFilters)}"
                            )*/

                            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
                            Log.v("MainActivity", "START_SCAN")
                        }
                    }
                }
            }
            awaitClose {
                MainScope().launch {
                    bluetoothLeScanner.stopScan(scanCallback)
                    delay(3000L)
                    SCAN_STATE.value = BleScannerScanState.STOP_SCAN

                    Log.v("MainActivity", "STOP_SCAN 4")
                }
            }
        }
    }

    companion object {
        val SCAN_STATE: MutableState<BleScannerScanState> =
            mutableStateOf(BleScannerScanState.START_SCAN)
        var scanFilters: MutableList<ScanFilter>? = null
    }
}

enum class BleScannerScanState { START_SCAN, STOP_SCAN }