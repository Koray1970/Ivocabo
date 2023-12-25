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
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.serko.ivocabo.BluetoothPermission
import com.serko.ivocabo.NotificationPermission
import com.serko.ivocabo.R
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.pages.bluetoothScanService
import com.serko.ivocabo.data.userViewModel
import com.serko.ivocabo.pages.dummyDevice
import com.serko.ivocabo.pages.metricDistance
import com.serko.ivocabo.pages.metricDistanceTextStyle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TrackMyDevice(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    val userviewModel = hiltViewModel<userViewModel>()
    val context = LocalContext.current.applicationContext

    val bluetoothPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        BluetoothPermission(context)
    val notificationPermission = NotificationPermission(context)
    if (!bluetoothPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            composeProgressStatus.value = false
            bluetoothPermissionStatus.second.launchMultiplePermissionRequest()
        }
    } else {
        if (!notificationPermission.first) {
            LaunchedEffect(Unit) {
                delay(300)
                composeProgressStatus.value = false
                notificationPermission.second.launchPermissionRequest()
            }
        } else {
            composeProgressStatus.value = true
            //val scope = rememberCoroutineScope()
            var deviceDetail by remember { mutableStateOf(dummyDevice) }
            var deviceIcon = R.drawable.t3_icon_32


            deviceDetail = userviewModel.getDeviceDetail(macaddress = macaddress!!)!!
            if (deviceDetail.devicetype != null) if (deviceDetail.devicetype == 2) deviceIcon =
                R.drawable.e9_icon_32
            val _macaddress = macaddress.uppercase(Locale.ROOT)

            LaunchedEffect(Unit) {

                if (!BleScanner.scanFilters.isNullOrEmpty()) {

                }
                userviewModel.getDeviceScanResult(_macaddress).collect {
                    metricDistance.value = it
                    composeProgressStatus.value = false
                }
            }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(containerColor = Color.Green,
                        shape = CircleShape,
                        onClick = {
                            MainScope().launch {
                                delay(300)
                                navController.navigate("devicedashboard/${deviceDetail.macaddress}")
                            }
                        },
                        content = {
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
                        text = context.getString(R.string.trackmydevicetitle), style = TextStyle(
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
                            text = _macaddress,
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
                            text = metricDistance.value,
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
        }
        BackHandler(true) {}
    }
}