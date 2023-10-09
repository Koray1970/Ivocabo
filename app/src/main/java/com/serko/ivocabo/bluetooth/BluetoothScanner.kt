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


class BluetoothScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    listOfMacaddress: MutableList<String>,
) {
    private val _listOfMacaddress = listOfMacaddress
    var evenState = MutableLiveData<BluetoothScannerState>(INITIATE)

    //var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null


    private val bluetoothpermission = if (Build.VERSION.SDK_INT > 30)
        android.Manifest.permission.BLUETOOTH_SCAN
    else
        android.Manifest.permission.BLUETOOTH_ADMIN

    companion object One {
        private val gson = Gson()
        private val TAG = BluetoothScanner::class.java.simpleName
        var currentRssi = MutableLiveData<Int?>()
        private lateinit var bluetoothLeScanner: BluetoothLeScanner
        private lateinit var scanSetting: ScanSettings
        private var scanList = mutableListOf<ScanFilter>()

        private val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                currentRssi.postValue(result?.rssi)
                //Log.v(TAG, "RSSI : ${result?.rssi}")
            }

            @SuppressLint("MissingPermission")
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                /*if (scanList.distinct().isNotEmpty()) {
                    val filterResult =
                        results?.filter { a -> scanList.any { g -> g.deviceAddress == a.device.address } == true }
                    var getdevv = emptyArray<String>()
                    var ii = 0
                    scanList.forEach {
                        getdevv.set(ii, it.deviceAddress!!)
                        ii++
                    }
                }*/
                //if(filterResult?.isNotEmpty()==true)

                Log.v(TAG, "scanList :  ${gson.toJson(scanList)}")
                Log.v(TAG, "results :  ${gson.toJson(results)}")

                if (!results.isNullOrEmpty()) {
                    var totalRSSI = results!!.sumOf { a -> a.rssi } / results!!.size
                    Log.v(TAG, "Avarage RSSI : $totalRSSI")
                    currentRssi.postValue(totalRSSI)

                } else {
                    currentRssi.postValue(0)
                    Log.v(TAG, "Avarage RSSI : is NULL")
                }
                /* results?.forEach {
                     Log.v(TAG, "Batch RSSI : ${it.rssi}")
                 }*/
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.v(TAG, "ERROR CODE : $errorCode")
            }
        }
    }


    fun getCurrentRSSI(): MutableLiveData<Int?> {
        return One.currentRssi
    }

    private var flwRSSI = MutableStateFlow<Int?>(0)
    fun getFlowCurrentRSSI(): MutableStateFlow<Int?> {
        MainScope().launch {
            flwRSSI.emit(One.currentRssi.value)
        }
        return flwRSSI
    }

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

            if (_listOfMacaddress.isNotEmpty()) {
                Log.v(TAG, "listOfMacaddress :  ${gson.toJson(_listOfMacaddress)}")
                if (scanList.isNotEmpty()) {
                    if (!scanList.any { a -> _listOfMacaddress.any { g -> g == a.deviceAddress } }) {
                        _listOfMacaddress?.forEach { mac ->
                            if (!scanList.any { a -> a.deviceAddress == mac }) {
                                scanList.add(
                                    ScanFilter.Builder()
                                        .setDeviceAddress(mac.uppercase(Locale.ROOT))
                                        .build()
                                )
                            }
                        }
                    }
                } else {
                    _listOfMacaddress?.forEach {
                        scanList.add(
                            ScanFilter.Builder()
                                .setDeviceAddress(it.uppercase(Locale.ROOT))
                                .build()
                        )
                    }
                }
                evenState.postValue(START_SCAN)
            }
        }



        try {
            MainScope().launch {
                delay(1000)
                bluetoothLeScanner?.startScan(scanList, scanSetting, scanCallback)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
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
