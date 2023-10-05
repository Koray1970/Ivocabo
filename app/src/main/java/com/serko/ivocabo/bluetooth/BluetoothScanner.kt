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
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.contentValuesOf
import androidx.lifecycle.MutableLiveData
import com.serko.ivocabo.bluetooth.BluetoothScannerState.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class BluetoothScannerState { INITIATE, START_SCAN, STOP_SCAN }

@SuppressLint("MissingPermission")
class BluetoothScanner @Inject constructor(@ApplicationContext private val context: Context) {
    var evenState = MutableLiveData<BluetoothScannerState>(INITIATE)
    var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var scanSetting: ScanSettings
    private lateinit var scanList: MutableList<ScanFilter>

    /*init {
        when (evenState.value) {
            START_SCAN -> {
                if (listOfMacaddress != null)
                    if (listOfMacaddress?.size!! > 0)
                        StartScan()
            }

            STOP_SCAN -> StopScan()
            else -> InitScan()
        }
    }*/

    fun InitScan() {
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        } else {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            scanList = mutableListOf<ScanFilter>()
            listOfMacaddress?.forEach {
                scanList.add(
                    ScanFilter.Builder()
                        .setDeviceAddress(it)
                        .build()
                )
            }
            evenState.postValue(START_SCAN)
        }
    }

    fun StartScan() {
        try {
            MainScope().launch {
                delay(1000)
                bluetoothLeScanner?.startScan(scanList, scanSetting, scanCallback)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun StopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.v("BLESCANRESULT", "RSSI : ${result?.rssi}")
            super.onScanResult(callbackType, result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach {
                Log.v("BLESCANRESULT", "Batch RSSI : ${it.rssi}")
            }
            super.onBatchScanResults(results)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.v("BLESCANRESULT", "ERROR CODE : $errorCode")
            super.onScanFailed(errorCode)
        }
    }


}


/*
class BluetoothScannerEventState {
    var evenState = mutableStateOf(BluetoothScannerState.INITIATE)
}*/
