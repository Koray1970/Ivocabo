package com.serko.ivocabo

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serko.ivocabo.bluetooth.BleScanFilterItem
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.bluetooth.BluetoothScanStates
import com.serko.ivocabo.bluetooth.BluetoothStatusObserver
import com.serko.ivocabo.bluetooth.IBluetoothStatusObserver
import com.serko.ivocabo.data.BleScanViewModel
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.pages.ComposeProgress
import com.serko.ivocabo.ui.theme.IvocaboTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

//koko
//koko@gmail.com
//123456
@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val userViewModel: UserViewModel by viewModels()
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

    /*fun RemoveDeviceTracking(macaddress: String) {
        val uVM: UserViewModel by viewModels()
        MainScope().launch {
            delay(300)
            val device = uVM.getDeviceDetail2(macaddress)
            if (device != null) {
                device.istracking = null
                uVM.addUpdateDevice(device)
            }
        }
    }*/

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
        val helper = Helper()
        val bluetoothStatusObserver = BluetoothStatusObserver(applicationContext)
        bluetoothStatusObserver.observeConnectivity().onEach {
            if (it == IBluetoothStatusObserver.BluetoothConnectivityStatus.UNAVAILABLE) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 2000)
            }
            println("BluetoothStatus : ${it.name}")
        }.launchIn(lifecycleScope)

        bleScanner = BleScanner(applicationContext)

        setContent {
            IvocaboTheme(
                darkTheme = false, dynamicColor = false
            ) {
                // A surface container using the 'background' color from the theme
                val scope = rememberCoroutineScope()
                val composeProgressDialogStatus = remember { mutableStateOf(false) }
                val deviceScanListResult =
                    bleScanViewModel.scanDevices().collectAsStateWithLifecycle(
                        initialValue = emptyList()
                    )
                LaunchedEffect(Unit) {

                    while (true) {
                        when (deviceScanListResult.value.isEmpty()) {
                            true -> {
                                if (BleScanner.scanStatus.value == BluetoothScanStates.SCANNING) {
                                    bleScanner.StopScanning()
                                    BleScanner.scanStatus.value = BluetoothScanStates.INIT
                                }
                            }

                            false -> {
                                var compareResult = 0
                                if (BleScanner.scanFilter.isNotEmpty()) {
                                    //compare scanfilter with devicescanlistresult
                                    if (BleScanner.scanFilter.size > deviceScanListResult.value.size)
                                        BleScanner.scanFilter.filter { a -> !a.onlytrackmydeviceevent }.toMutableList()
                                            .removeIf { a -> deviceScanListResult.value.none { g -> g.uppercase() == a.macaddress.uppercase() } }
                                }
                                deviceScanListResult.value.onEach { a ->
                                    if (BleScanner.scanFilter.none { g -> g.macaddress.uppercase() == a.uppercase() }) {
                                        val deviceDetail =
                                            userViewModel.getDeviceDetail2(a.uppercase())
                                        BleScanner.scanFilter.add(
                                            BleScanFilterItem(
                                                name = deviceDetail?.name ?: "",
                                                macaddress = a.uppercase(),
                                                notifyid = helper.getRandomInt(),
                                                stimulable = true
                                            )
                                        )
                                    }
                                }
                                /*if (BleScanner.scanResults.isNotEmpty()) {
                                    BleScanner.scanResults.removeIf { a -> deviceScanListResult.value.none { c -> c.uppercase() == a.macaddress.uppercase() } }
                                }*/
                                if (BleScanner.scanStatus.value == BluetoothScanStates.INIT) {
                                    bleScanner.StartScanning()
                                    BleScanner.scanStatus.value = BluetoothScanStates.SCANNING
                                }
                            }
                        }
                        delay(1000L)
                    }

                }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    /* SideEffect {
                         scope.launch {
                             while (true) {
                                 if (BleScanner.scanResults.isNotEmpty()) {
                                     BleScanner.scanResults.onEach { a ->
                                         if (a.disconnectedCounter >= 11) {
                                             notifService.showNotification("", a.macaddress)
                                         }
                                     }
                                 }
                                 delay(2000)
                             }
                         }
                     }*/
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
