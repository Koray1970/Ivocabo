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
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.toUpperCase
import com.serko.ivocabo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import javax.inject.Inject

interface IBluetoothScanService {
    fun bluetoothScanner(): Flow<MutableList<BluetoothScannerResult>?>
}

class BluetoothScanService @Inject constructor(@ApplicationContext private val context: Context) :
    IBluetoothScanService {
    var stopLocationJob = MutableStateFlow<Boolean>(false)
    val flowListOfMacaddress = flowOf<MutableList<String>>()
    var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanSetting: ScanSettings
    private var scanList = mutableListOf<ScanFilter>()
    private var resultList = mutableListOf<BluetoothScannerResult>()

    @SuppressLint("MissingPermission")
    override fun bluetoothScanner(): Flow<MutableList<BluetoothScannerResult>?> = callbackFlow {
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, context.getString(R.string.enablebluetooth), Toast.LENGTH_LONG)
                .show()
        } else {
            var scanCallback: ScanCallback? = null
            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(3000)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            scanList = mutableListOf<ScanFilter>()
            flowListOfMacaddress.flowOn(Dispatchers.Default).collect { mc ->
                if (mc.isNotEmpty()) {
                    mc.forEach { i ->
                        scanList.add(
                            ScanFilter.Builder().setDeviceAddress(i.uppercase(Locale.ROOT)).build()
                        )
                    }
                    scanList = scanList.distinctBy { a -> a.deviceAddress }.toMutableList()
                } else {
                    bluetoothLeScanner?.stopScan(scanCallback)
                }
                if (resultList.isNotEmpty()) {
                    resultList =
                        resultList.filter { a -> scanList.any { s -> s.deviceAddress == a.macaddress } }
                            .toMutableList()
                }
            }

            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!

            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result ?: return
                }

                @SuppressLint("MissingPermission")
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    results ?: return
                    if (results.isNotEmpty()) {
                        if (resultList.isNotEmpty()) {

                        } else {
                            results.forEach { a ->
                                var findScanResult = resu
                                resultList.add(
                                    BluetoothScannerResult(
                                        a.device.address,
                                        a.rssi,
                                        1,
                                        BluetoothScannerCallbackStatus.SCANNING
                                    )
                                )
                            }
                        }
                    } else {
                        //arama sonucu bos dundu ise
                        if (resultList.isNotEmpty()) {
                            resultList.forEach { a ->
                                var flt =
                                    resultList.filter { g -> scanList.any { h -> h.deviceAddress == g.macaddress } }
                                if(flt.isNotEmpty()) {
                                    if (a.countOfDisconnected == null) a.countOfDisconnected = 0
                                    a.countOfDisconnected = a.countOfDisconnected!! + 1

                                    if (a.countOfDisconnected!! >= 10)
                                        a.callbackStatus =
                                            BluetoothScannerCallbackStatus.DEVICE_NOT_FOUND
                                    else
                                        a.callbackStatus = BluetoothScannerCallbackStatus.SCANNING
                                }
                                else
                                    resultList.removeAll(flt)
                            }
                        } else {
                            scanList.forEach { a ->
                                resultList.add(
                                    BluetoothScannerResult(
                                        a.deviceAddress,
                                        null,
                                        1,
                                        BluetoothScannerCallbackStatus.SCANNING
                                    )
                                )
                            }
                        }
                    }
                }
            }
            if (scanList.isNotEmpty()) {
                delay(100)
                bluetoothLeScanner.startScan(scanList, scanSetting, scanCallback)
            }

            awaitClose {
                bluetoothLeScanner?.stopScan(scanCallback)
            }
            stopLocationJob.collect {
                if (it)
                    bluetoothLeScanner?.stopScan(scanCallback)
            }
        }
    }
}