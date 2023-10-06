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
import com.serko.ivocabo.bluetooth.BluetoothScannerState.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class BluetoothScannerState { INITIATE, START_SCAN, STOP_SCAN }


class BluetoothScanner @Inject constructor(@ApplicationContext private val context: Context) {
    var evenState = MutableLiveData<BluetoothScannerState>(INITIATE)
    var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null


    private val bluetoothpermission = if (Build.VERSION.SDK_INT > 30)
        android.Manifest.permission.BLUETOOTH_SCAN
    else
        android.Manifest.permission.BLUETOOTH_ADMIN

    companion object One {
        private val TAG = BluetoothScanner::class.java.simpleName
        var currentRssi=MutableLiveData<Int?>()
        private lateinit var bluetoothLeScanner: BluetoothLeScanner
        private lateinit var scanSetting: ScanSettings
        private var scanList = mutableListOf<ScanFilter>()
        private val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                currentRssi.postValue (result?.rssi)
                Log.v(TAG, "RSSI : ${result?.rssi}")
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                if (!results.isNullOrEmpty()) {
                    var totalRSSI = results!!.sumOf { a -> a.rssi } / results!!.size
                    Log.v(TAG, "Avarage RSSI : $totalRSSI")
                    currentRssi.postValue(totalRSSI)
                }
                else{
                    currentRssi.postValue(null)
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
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!

            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setReportDelay(3000)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Bluetooth scan permission is denied!", Toast.LENGTH_LONG)
                    .show()
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

    @SuppressLint("MissingPermission")
    fun StopScan() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    bluetoothpermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Bluetooth scan permission is denied!", Toast.LENGTH_LONG)
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
