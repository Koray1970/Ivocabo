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
import com.serko.ivocabo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
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
    //fun bluetoothScannerStart()

    fun bluetoothScannerResults(): Flow<MutableList<BluetoothScannerBatchResult>?>
    //fun bluetoothScannerStop()
}

@Singleton
class BluetoothScanService @Inject constructor(@ApplicationContext private val context: Context) :
    IBluetoothScanService {
    private val gson = Gson()
    var scanJonState = MutableStateFlow<Boolean>(false)
    var scanList = mutableListOf<ScanFilter>()

    private var bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanCallback:ScanCallback


    private val scanSetting = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(3000)
        //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .build()

    @SuppressLint("MissingPermission")
    override fun bluetoothScannerResults(): Flow<MutableList<BluetoothScannerBatchResult>?> =
        callbackFlow {

            bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter?.isEnabled == false) {
                Toast.makeText(
                    context,
                    context.getString(R.string.enablebluetooth),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            else {
                bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
                //setScanList()
                delay(500)

                scanCallback = object : ScanCallback() {
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
                        trySend(rr).isSuccess
                    }

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        Log.v("MainActivity", "onScanFailed: ${errorCode} ")
                    }
                }


                when (scanJonState.value) {
                    true -> {
                        Log.v("MainActivity","bluetoothLeScanner.startScan")
                        bluetoothLeScanner.startScan(scanList, scanSetting, scanCallback)
                    }

                    else -> {
                        Log.v("MainActivity","bluetoothLeScanner.stopScan")
                        bluetoothLeScanner.stopScan(scanCallback)
                    }
                }
                awaitClose {
                    Log.v("MainActivity","bluetoothLeScanner.stopScan at awaitClose")
                    bluetoothLeScanner.stopScan(scanCallback)
                }
            }
        }


}