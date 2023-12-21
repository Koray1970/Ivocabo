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
import com.serko.ivocabo.data.userViewModel
import com.serko.ivocabo.pages.ComposeProgress
import com.serko.ivocabo.pages.gson
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

    companion object {
        lateinit var bleScanner: BleScanner
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
                    BleScanner.scanFilters.removeIf { a -> it.none { g -> g == a.deviceAddress } }
                    it.forEach { a ->
                        BleScanner.scanFilters.add(ScanFilter.Builder().setDeviceAddress(a).build())
                    }
                    BleScanner.SCAN_STATE.value = BleScannerScanState.START_SCAN
                    bleScanner.getScanResults().collect { h ->
                        Log.v("MainActivity", gson.toJson(h))
                    }
                } else
                    BleScanner.SCAN_STATE.value = BleScannerScanState.STOP_SCAN
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
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
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
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
