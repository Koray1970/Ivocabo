package com.serko.ivocabo.bluetooth

import android.Manifest
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
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.contentValuesOf
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.serko.ivocabo.R
import com.serko.ivocabo.bluetooth.BluetoothScannerState.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


enum class BluetoothScannerState { INITIATE, START_SCAN, STOP_SCAN }
enum class BluetoothScannerCallbackStatus(s: Int) {
    SCANNING(0), DEVICE_NOT_FOUND(1), CONNECTING(2), CONNECTION_LOST(
        3
    )
}

data class BluetoothScannerResult(
    val macaddress: String?,
    var rssi: Int?,
    var countOfDisconnected: Int?,
    var callbackStatus: BluetoothScannerCallbackStatus?
)

class BluetoothScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    var listOfMacaddress: MutableList<String>,
) {
    var evenState = MutableLiveData<BluetoothScannerState>(INITIATE)

    //var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null


    private val bluetoothpermission = if (Build.VERSION.SDK_INT > 30)
        Manifest.permission.BLUETOOTH_SCAN
    else
        Manifest.permission.BLUETOOTH_ADMIN

    companion object One {
        private val TAG = BluetoothScanner::class.java.simpleName
        private var _listOfMacaddress = mutableListOf<String>()

        private val gson = Gson()

        var currentRssi = MutableLiveData<Int?>()
        private lateinit var bluetoothLeScanner: BluetoothLeScanner
        private lateinit var scanSetting: ScanSettings
        private var scanList = mutableListOf<ScanFilter>()

        val bleScanResultList = MutableLiveData<MutableList<BluetoothScannerResult>>()

        private val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                //currentRssi.postValue(result?.rssi)
                //Log.v(TAG, "RSSI : ${result?.rssi}")
            }

            @SuppressLint("MissingPermission")
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                var localScanResults = mutableListOf<BluetoothScannerResult>()
                if (bleScanResultList.value?.size!! > 0) {
                    localScanResults = bleScanResultList.value!!
                }
                val _results = results?.distinctBy { a -> a.device.address }
                _listOfMacaddress.forEach { mm ->
                    var bsr = BluetoothScannerResult(mm, null, null, null)
                    if (localScanResults.size > 0) {
                        if (localScanResults.any { a -> a.macaddress == mm }) {
                            bsr = localScanResults.last { a -> a.macaddress == mm }
                            localScanResults.removeIf { a -> a.macaddress == mm }
                        }
                    }
                    if (results?.isNotEmpty() == true) {
                        var lastresult = results?.last { a -> a.device.address == mm }
                        if (lastresult != null) {
                            bsr.rssi = lastresult.rssi
                            bsr.callbackStatus = BluetoothScannerCallbackStatus.CONNECTING
                            if (bsr.countOfDisconnected != null) {
                                bsr.countOfDisconnected = null
                            }
                        } else {
                            bsr.countOfDisconnected = bsr.countOfDisconnected!! + 1
                            if (bsr.countOfDisconnected!! > 10) {
                                bsr.rssi = null
                                bsr.callbackStatus = BluetoothScannerCallbackStatus.CONNECTION_LOST
                            }
                        }
                    } else {
                        if (bsr.countOfDisconnected == null) bsr.countOfDisconnected = 0
                        bsr.countOfDisconnected = bsr.countOfDisconnected!! + 1
                        if (bsr.countOfDisconnected!! > 10) {
                            bsr.rssi = null
                            bsr.callbackStatus = BluetoothScannerCallbackStatus.CONNECTION_LOST
                        }
                    }
                    localScanResults.add(bsr)
                }
                bleScanResultList.postValue(localScanResults)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.v(TAG, "ERROR CODE : $errorCode")
            }
        }
    }

    fun setListofMacaddress() {
        if (listOfMacaddress.isNotEmpty())
            listOfMacaddress = listOfMacaddress.distinct().toMutableList()
        One._listOfMacaddress = listOfMacaddress
    }

    fun getCurrentRSSI(): MutableLiveData<Int?> {
        return One.currentRssi
    }

    fun getBluetoothScannerResults(): MutableLiveData<MutableList<BluetoothScannerResult>> {
        return One.bleScanResultList
    }


    /* private var flwRSSI = MutableStateFlow<Int?>(0)
     fun getFlowCurrentRSSI(): MutableStateFlow<Int?> {
         MainScope().launch {
             flwRSSI.emit(One.currentRssi.value)
         }
         return flwRSSI
     }*/

    @SuppressLint("MissingPermission")
    fun StartScan() {

        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, context.getString(R.string.enablebluetooth), Toast.LENGTH_LONG)
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

            if (listOfMacaddress.isNotEmpty()) {
                listOfMacaddress = listOfMacaddress.distinct().toMutableList()
                setListofMacaddress()
                scanList = mutableListOf<ScanFilter>()
                listOfMacaddress?.forEach {
                    scanList.add(
                        ScanFilter.Builder()
                            .setDeviceAddress(it.uppercase(Locale.ROOT))
                            .build()
                    )
                }
                try {
                    MainScope().launch {
                        delay(1000)
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_SCAN
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }
                        bluetoothLeScanner?.startScan(scanList, scanSetting, scanCallback)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun StopScan() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    context,
                    "Bluetooth scan permission is denied!",
                    Toast.LENGTH_LONG
                )
                    .show()
                return
            }
            bluetoothLeScanner.stopScan(scanCallback)
            Toast.makeText(context, "Bluetooth scan stop", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
        }
    }


}


/*
class BluetoothScannerEventState {
    var evenState = mutableStateOf(BluetoothScannerState.INITIATE)
}*/
