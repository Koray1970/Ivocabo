package com.serko.ivocabo

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DismissValue.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.bluetooth.BleScannerScanState
import com.serko.ivocabo.bluetooth.BluetoothActivity
import com.serko.ivocabo.bluetooth.BluetoothStatusObserver
import com.serko.ivocabo.bluetooth.IBluetoothStatusObserver
import com.serko.ivocabo.bluetooth.ScanningDeviceItem
import com.serko.ivocabo.data.BleScanViewModel
import com.serko.ivocabo.data.ScanResultItem
import com.serko.ivocabo.data.userViewModel
import com.serko.ivocabo.pages.ComposeProgress
import com.serko.ivocabo.pages.gson
import com.serko.ivocabo.pages.helper
import com.serko.ivocabo.ui.theme.IvocaboTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

//koko
//koko@gmail.com
//123456
@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val userViewModel: userViewModel by viewModels()
    val bleScanViewModel: BleScanViewModel by viewModels()

    companion object {
        lateinit var bleScanner: BleScanner
    }

    override fun onLowMemory() {
        Toast.makeText(applicationContext, "Uygulama onLowMemory!!", Toast.LENGTH_LONG).show()
        super.onLowMemory()
    }

    override fun onResume() {
        Toast.makeText(applicationContext, "Uygulama onResume!!", Toast.LENGTH_LONG).show()
        super.onResume()
    }

    override fun onPause() {
        Toast.makeText(applicationContext, "Uygulama onPause!!", Toast.LENGTH_LONG).show()
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //applicationContext.deleteDatabase(applicationContext.getString(R.string.dbname))

        hideSystemUI()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        val bluetoothStatusObserver = BluetoothStatusObserver(applicationContext)
        bluetoothStatusObserver.observeConnectivity().onEach {
            if (it == IBluetoothStatusObserver.BluetoothConnectivityStatus.UNAVAILABLE) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 2000)
            }
            println("BluetoothStatus : ${it.name}")
        }.launchIn(lifecycleScope)
        var kk = 0
        bleScanner = BleScanner(applicationContext)
        MainScope().launch {
            userViewModel.getScanDeviceList().flowOn(Dispatchers.Default).cancellable().collect {
                Log.v("MainActivity", "kk=${kk++}")
                if (it.isNotEmpty()) {
                    if (BleScanner.scanFilters.isNullOrEmpty())
                        BleScanner.scanFilters = mutableListOf<ScanFilter>()
                    if (BleScanner.scanFilters!!.size > 0)
                        BleScanner.scanFilters!!.removeIf { a -> it.none { g -> g == a.deviceAddress } }
                    it.forEach { a ->
                        BleScanner.scanFilters!!.add(
                            ScanFilter.Builder().setDeviceAddress(a).build()
                        )
                    }
                    BleScanner.SCAN_STATE.value = BleScannerScanState.START_SCAN

                } else {
                    BleScanner.scanFilters = null
                    BleScanner.SCAN_STATE.value = BleScannerScanState.STOP_SCAN
                }
                bleScanner.getScanResults().collect { sr ->
                    Log.v("MainActivity 1", gson.toJson(sr))
                    if (BleScanner.scanFilters != null) {
                        if (sr != null) {
                            if (sr.isNotEmpty()) {
                                sr.forEach { a ->
                                    if (BleScanner.scanFilters!!.any { v -> v.deviceAddress == a.device.address }) {
                                        bleScanViewModel.addAndUpdateScanResultList(
                                            ScanResultItem(
                                                macaddress = a.device.address,
                                                rssi = a.rssi,
                                                metricvalue = helper.CalculateRSSIToMeter(a.rssi)!!,
                                                disconnectedcounter = null,
                                            )
                                        )
                                    } else {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //val bluetoothActivity = BluetoothActivity(applicationContext)

        setContent {
            IvocaboTheme(
                darkTheme = false, dynamicColor = false
            ) {
                // A surface container using the 'background' color from the theme
                val composeProgressDialogStatus = remember { mutableStateOf(false) }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(composeProgressDialogStatus)
                    ComposeProgress(dialogshow = composeProgressDialogStatus)
                }
            }
        }
    }

    private fun hideSystemUI() {

        //Hides the ugly action bar at the top
        actionBar?.hide()

        //Hide the status bars

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}


/*@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
IvocaboTheme {
Signup(navController)
}
}*/
