package com.serko.ivocabo.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.serko.ivocabo.BluetoothPermission
import com.serko.ivocabo.Helper
import com.serko.ivocabo.R
import com.serko.ivocabo.data.BleScanViewModel
import com.serko.ivocabo.data.UserViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FindMyDevice(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    composeProgressStatus.value = true
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()
    val bluetoothPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        BluetoothPermission(context)

    if (!bluetoothPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            composeProgressStatus.value = false
            bluetoothPermissionStatus.second.launchMultiplePermissionRequest()
        }
    } else {
        composeProgressStatus.value = true
        val mMacaddress = macaddress!!.uppercase(Locale.ROOT)
        val bleScanViewModel = hiltViewModel<BleScanViewModel>()

        val helper = Helper()
        var deviceDetail by remember { mutableStateOf(dummyDevice) }
        var deviceIcon = R.drawable.t3_icon_32

        val _deviceDetail = userviewModel.getDeviceDetail(macaddress = macaddress)
            .collectAsStateWithLifecycle(initialValue = dummyDevice)



        val metricValue = remember { mutableStateOf("") }
        val getScanResult =
            bleScanViewModel.getCurrentDeviceResult(mMacaddress)
                .collectAsStateWithLifecycle(initialValue = null)

        LaunchedEffect(Unit) {
            delay(100)
            bleScanViewModel.addItemToBleScannerFilter(_deviceDetail.value, false)
            deviceDetail = _deviceDetail.value
            if (deviceDetail.devicetype != null) if (deviceDetail.devicetype == 2) deviceIcon =
                R.drawable.e9_icon_32
            while (true) {
                if (getScanResult.value != null) {
                    if (getScanResult.value!!.rssi != null)
                        metricValue.value =
                            helper.CalculateRSSIToMeter(getScanResult.value!!.rssi) + "mt"
                }
                delay(2000L)
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(containerColor = Color.Green, shape = CircleShape, onClick = {
                    MainScope().launch {
                        delay(300)
                        navController.navigate("devicedashboard/${deviceDetail.macaddress}")
                    }
                }, content = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = ""
                    )
                })
            }, floatingActionButtonPosition = FabPosition.Start
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = context.getString(R.string.findmydevicetitle), style = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Normal,
                        fontSize = 32.sp
                    )
                )
                Spacer(modifier = Modifier.height(40.dp))
                Image(
                    painter = painterResource(id = deviceIcon),
                    modifier = Modifier.size(120.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(40.dp))
                Column(modifier = Modifier.wrapContentWidth()) {
                    Text(
                        text = context.getString(R.string.devicename),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                    Text(
                        text = deviceDetail.name,
                        style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = context.getString(R.string.macaddress),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )

                    Text(
                        text = mMacaddress,
                        style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.distancefromdevice),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    )
                    Text(
                        text = metricValue.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.DarkGray),
                        style = metricDistanceTextStyle.value
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.distancefromdevicewarning),
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.LightGray
                        )
                    )
                }
            }
        }
        BackHandler(true) {}
        composeProgressStatus.value = false
    }

}