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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.contentValuesOf
import androidx.lifecycle.MutableLiveData
import com.serko.ivocabo.bluetooth.BluetoothScannerState.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class BluetoothScannerState { INITIATE, START_SCAN, STOP_SCAN }


class BluetoothScanner @Inject constructor(@ApplicationContext private val context: Context) {
    private val TAG = BluetoothScanner::class.java.simpleName
    var evenState = MutableLiveData<BluetoothScannerState>(INITIATE)
    var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var scanSetting: ScanSettings
    private lateinit var scanList: MutableList<ScanFilter>

    private val bluetoothpermission=if(Build.VERSION.SDK_INT>30)
        android.Manifest.permission.BLUETOOTH_SCAN
    else
        android.Manifest.permission.BLUETOOTH_ADMIN


    fun InitScan() {
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Enable bluetooth device!", Toast.LENGTH_LONG).show()
                return
            }
            context.startActivity(enableBtIntent)
        } else {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
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

    val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.v(TAG, "RSSI : ${result?.rssi}")

        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach {
                Log.v(TAG, "Batch RSSI : ${it.rssi}")
            }
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.v(TAG, "ERROR CODE : $errorCode")
            super.onScanFailed(errorCode)
        }
    }


    fun StartScan() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Bluetooth scan permission is denied!", Toast.LENGTH_LONG).show()
                return
            }
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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Bluetooth scan permission is denied!", Toast.LENGTH_LONG).show()
                return
            }
            InitScan()
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Toast.makeText(context, "Error:${e.message}", Toast.LENGTH_LONG).show()
        }
    }


}


/*
class BluetoothScannerEventState {
    var evenState = mutableStateOf(BluetoothScannerState.INITIATE)
}*/
