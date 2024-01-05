package com.serko.ivocabo.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.serko.ivocabo.BluetoothPermission
import com.serko.ivocabo.MainActivity
import com.serko.ivocabo.NotificationPermission
import com.serko.ivocabo.R
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.bluetooth.BluetoothScanStates
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.location.AppFusedLocationRepo
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeviceDashboard(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),

    //locationViewModel: LocationViewModel = hiltViewModel()
) {
    composeProgressStatus.value = true
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()
    val bluetoothPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        BluetoothPermission(context)

    if (!bluetoothPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            bluetoothPermissionStatus.second.launchMultiplePermissionRequest()
        }
        composeProgressStatus.value = false
    } else {

        val notificationPermission = NotificationPermission(context)
        if (!notificationPermission.first) {
            LaunchedEffect(Unit) {
                delay(300)
                notificationPermission.second.launchPermissionRequest()
            }
            composeProgressStatus.value = false
        } else {
            val scope = rememberCoroutineScope()
            //var deviceDetail by remember { mutableStateOf(dummyDevice) }
            var chkNotificationCheckState by remember { mutableStateOf(false) }

            var chkMissingCheckState by remember { mutableStateOf(false) }
            var geopoint = GeoPoint(0.0, 0.0)
            val mapMarkerState = rememberMarkerState(geoPoint = geopoint)
            var mapProperties by remember { mutableStateOf(DefaultMapProperties) }
            val cameraState = rememberCameraState {
                geoPoint = geopoint
                zoom = 19.0 // optional, default is 5.0
            }
            val appFusedLocationRepo = AppFusedLocationRepo(context)
            val getLocation = appFusedLocationRepo.startCurrentLocation()
                .collectAsStateWithLifecycle(initialValue = LatLng(0.0, 0.0))
            val deviceDetail = userviewModel.getDeviceDetail(macaddress = macaddress!!)
                .collectAsStateWithLifecycle(initialValue = dummyDevice)
            scope.launch {
                delay(1000)
                if (deviceDetail.value.istracking != null) chkNotificationCheckState =
                    deviceDetail.value.istracking == true
                if (deviceDetail.value.ismissing != null) chkMissingCheckState =
                    deviceDetail.value.ismissing == true
                while (true) {
                    if (deviceDetail.value.latitude == "null")
                        deviceDetail.value.latitude = getLocation.value.latitude.toString()
                    if (deviceDetail.value.longitude == "null")
                        deviceDetail.value.longitude = getLocation.value.longitude.toString()
                    geopoint = GeoPoint(
                        deviceDetail.value.latitude!!.toDouble(),
                        deviceDetail.value.longitude!!.toDouble()
                    )
                    cameraState.geoPoint = geopoint
                    mapMarkerState.geoPoint = geopoint

                    mapProperties = mapProperties.copy(isTilesScaledToDpi = true)
                        .copy(tileSources = TileSourceFactory.MAPNIK)
                        .copy(isEnableRotationGesture = false)
                        .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER)
                    if (getLocation.value.latitude > 0) {
                        break
                    }
                    delay(1000)
                }
                composeProgressStatus.value = false
            }
            //end:Map Properties

            Scaffold(bottomBar = {
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Dashboard.route) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_home_24),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_person_24),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Preference.route) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_settings_24),
                                contentDescription = null
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(containerColor = Color.Green,
                            shape = CircleShape,
                            onClick = {
                                navController.navigate(Screen.Dashboard.route)
                            },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                    contentDescription = ""
                                )
                            })
                    },
                )
            }) { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    OpenStreetMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        cameraState = cameraState,
                        properties = mapProperties
                    ) { Marker(state = mapMarkerState) }
                    HorizontalDivider(thickness = 3.dp, modifier = Modifier.fillMaxWidth())
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.devicedashboardbackground))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = deviceDetail.value.name,
                                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${deviceDetail.value.macaddress.uppercase(Locale.ROOT)} ${deviceDetail.value.registerdate}",
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Light),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                ElevatedButton(modifier = Modifier
                                    .weight(1f)
                                    .alpha(.7f)
                                    .fillMaxHeight(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonColors(
                                        containerColor = Color.Black,
                                        disabledContainerColor = Color.LightGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.DarkGray
                                    ),
                                    onClick = {
                                        navController.navigate("trackmydevice/${deviceDetail.value.macaddress}")
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_track_changes_24),
                                        tint = Color.Red,
                                        modifier = Modifier.size(48.dp),
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val txttrack = String.format(
                                        context.getString(R.string.tracking),
                                        "\n${deviceDetail.value.name}"
                                    )
                                    Text(text = txttrack, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                ElevatedButton(modifier = Modifier
                                    .weight(1f)
                                    .alpha(.7f)
                                    .fillMaxHeight(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonColors(
                                        containerColor = Color.Black,
                                        disabledContainerColor = Color.LightGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.DarkGray
                                    ),
                                    onClick = {
                                        navController.navigate("findmydevice/${deviceDetail.value.macaddress}")
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_wifi_find_24),
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .alpha(.7f),
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val txtfindmydevice = String.format(
                                        context.getString(R.string.findmydevice),
                                        "\n${deviceDetail.value.name}"
                                    )
                                    Text(text = txtfindmydevice, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .alpha(.7f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardColors(
                                        containerColor = Color.Black,
                                        disabledContainerColor = Color.LightGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.DarkGray
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Switch(checked = chkNotificationCheckState,
                                            onCheckedChange = { cc ->
                                                //val mmc = macaddress!!.uppercase(Locale.ROOT)
                                                //track switch onchecked
                                                MainScope().launch {
                                                    val getloc =
                                                        AppFusedLocationRepo(context).startCurrentLocation()
                                                            .cancellable().first()
                                                    delay(100)
                                                    if (!cc) {
                                                        //workManager.cancelWorkById(trackWorkRequest.id)
                                                        deviceDetail.value.istracking = null
                                                    } else {
                                                        //workManager.enqueue(trackWorkRequest)
                                                        deviceDetail.value.istracking = true
                                                        deviceDetail.value.ismissing = null
                                                    }

                                                    //MainActivity.bleScanner.StopScanning()

                                                    deviceDetail.value.longitude =
                                                        getloc?.longitude.toString()
                                                    deviceDetail.value.latitude =
                                                        getloc?.latitude.toString()
                                                    userviewModel.addUpdateDevice(deviceDetail.value)
                                                        .flowOn(Dispatchers.Default).cancellable()
                                                        .collect { result ->
                                                            when (result) {
                                                                null -> {
                                                                    doNothing()
                                                                }

                                                                else -> {
                                                                    if (result.resultFlag.flag == 1) {
                                                                        if (cc) {
                                                                            chkMissingCheckState =
                                                                                false
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Tracking servisi açıldı.",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()

                                                                        } else {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Tracking servisi kapatıldı.",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                        }

                                                                    } else {
                                                                        if (result.error != null) {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Error : ${result.error!!.code}, Exception : ${result.error!!.exception} ",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                }
                                                chkNotificationCheckState = cc
                                            },
                                            thumbContent = if (chkNotificationCheckState) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                                    )
                                                }
                                            } else {
                                                null
                                            })
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = context.getString(R.string.trackingnotification),
                                            color = Color.White,
                                            style = TextStyle(textAlign = TextAlign.Center)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .alpha(.7f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardColors(
                                        containerColor = Color.Black,
                                        disabledContainerColor = Color.LightGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.DarkGray
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Switch(checked = chkMissingCheckState, onCheckedChange = {
                                            MainScope().launch {
                                                val getloc =
                                                    AppFusedLocationRepo(context).startCurrentLocation()
                                                        .cancellable().first()
                                                delay(100)
                                                if (!it) deviceDetail.value.ismissing = null
                                                else {
                                                    deviceDetail.value.istracking = null
                                                    deviceDetail.value.ismissing = true
                                                }
                                                deviceDetail.value.longitude =
                                                    getloc?.longitude.toString()
                                                deviceDetail.value.latitude =
                                                    getloc?.latitude.toString()
                                                userviewModel.addUpdateDevice(deviceDetail.value)
                                                    .flowOn(Dispatchers.Default).cancellable()
                                                    .collect { result ->
                                                        when (result) {
                                                            null -> {
                                                                doNothing()
                                                            }

                                                            else -> {
                                                                if (result.resultFlag.flag == 1) {
                                                                    if (it) {
                                                                        chkNotificationCheckState =
                                                                            false
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Missing Listesine Eklendi",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    } else Toast.makeText(
                                                                        context,
                                                                        "Missing Listesinden Çıkarıldı",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                } else {
                                                                    if (result.error != null) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Error : ${result.error!!.code}, Exception : ${result.error!!.exception} ",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }

                                            chkMissingCheckState = it
                                        }, thumbContent = if (chkMissingCheckState) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                                )
                                            }
                                        } else {
                                            null
                                        })
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = context.getString(R.string.missing),
                                            color = Color.White,
                                            style = TextStyle(textAlign = TextAlign.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}