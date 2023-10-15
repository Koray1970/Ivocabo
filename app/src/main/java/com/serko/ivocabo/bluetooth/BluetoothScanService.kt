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
import com.google.gson.Gson
import com.serko.ivocabo.DoNothing
import com.serko.ivocabo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

//enum class BluetoothScannerState { INITIATE, START_SCAN, STOP_SCAN }
enum class BluetoothScannerCallbackStatus() {
    SCANNING, DEVICE_NOT_FOUND, CONNECTING, CONNECTION_LOST
}

data class BluetoothScannerResult(
    val macaddress: String?,
    var rssi: Int?,
    var countOfDisconnected: Int?,
    var callbackStatus: BluetoothScannerCallbackStatus?
)

data class BluetoothScannerBatchResult(
    val macaddress: String?,
    var rssi: Int?
)

interface IBluetoothScanService {
    fun bluetoothScannerStart()

    //fun bluetoothScannerResults(): Flow<MutableList<BluetoothScannerBatchResult>?>
    fun bluetoothScannerStop()
}

@Singleton
class BluetoothScanService @Inject constructor(@ApplicationContext private val context: Context) :
    IBluetoothScanService {
    private val gson = Gson()
    var stopLocationJob = MutableStateFlow<Boolean>(false)
    var flowListOfMacaddress = flowOf<MutableList<String>>()

    //var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanSetting: ScanSettings
    private var scanList: MutableList<ScanFilter> = mutableListOf<ScanFilter>()
    var resultList = flowOf(mutableListOf<BluetoothScannerBatchResult>())
    private var scanResults = flowOf(mutableListOf<ScanResult>())

    @SuppressLint("MissingPermission")
    suspend fun setScanList() {
        flowListOfMacaddress.flowOn(Dispatchers.Default).collect { mc ->
            if (mc.isNotEmpty()) {
                Log.v("MainActivity", "FlowListOfMacAddress: ${gson.toJson(mc)}")
                scanList = mutableListOf<ScanFilter>()
                mc.forEach { i ->
                    scanList!!.add(
                        ScanFilter.Builder().setDeviceAddress(i.uppercase(Locale.ROOT))
                            .build()
                    )
                }
            } else {
                scanList = mutableListOf<ScanFilter>()
                bluetoothLeScanner?.stopScan(scanCallback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun bluetoothScannerStart() {
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(
                context,
                context.getString(R.string.enablebluetooth),
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!

            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(3000)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

            MainScope().launch {
                setScanList()
                delay(2000)
                bluetoothLeScanner.startScan(scanList, scanSetting, scanCallback)
            }
        }
    }

    /*@SuppressLint("MissingPermission")
    override fun bluetoothScannerResults(): Flow<MutableList<BluetoothScannerBatchResult>?> =
        callbackFlow {
            setScanList()
            delay(100)
            bluetoothScannerStart()
            when (scanResults.first().size) {
                0 -> {
                    DoNothing()
                }

                else -> {
                    scanResults.first().filter { a -> resultList.any { g -> g.macaddress?.uppercase() == a.device.address.uppercase() } }
                        .forEach { cc ->
                            resultList.first { a -> a.macaddress == cc.device.address }.rssi =
                                cc.rssi
                        }
                }
            }

            trySend(resultList).isSuccess
            awaitClose {
                bluetoothScannerStop()
            }
            stopLocationJob.collect {
                if (it)
                    bluetoothScannerStop()
            }
        }*/

    @SuppressLint("MissingPermission")
    override fun bluetoothScannerStop() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results ?: return
            var rr = mutableListOf<BluetoothScannerBatchResult>()
            results.filter { a -> scanList.any { g -> g.deviceAddress?.uppercase() == a.device.address.uppercase() } }
                .forEach { cc ->
                    rr.add(BluetoothScannerBatchResult(cc.device.address, cc.rssi))
                }
            resultList = flowOf(rr)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.v("MainActivity", "onScanFailed: ${errorCode} ")
        }
    }

}