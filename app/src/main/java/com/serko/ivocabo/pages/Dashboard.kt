package com.serko.ivocabo.pages

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberSwipeToDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.serko.ivocabo.BluetoothPermission
import com.serko.ivocabo.DeviceFormHelper
import com.serko.ivocabo.FormDeviceItem
import com.serko.ivocabo.Helper
import com.serko.ivocabo.LocationPermission
import com.serko.ivocabo.NotificationPermission
import com.serko.ivocabo.R
import com.serko.ivocabo.data.RMEventStatus
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.location.AppFusedLocationRepo
import com.serko.ivocabo.location.AppFusedLocationState
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Dashboard(
    navController: NavController = rememberNavController(),
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    composeProgressStatus.value = true
    val gson = Gson()
    val helper = Helper()
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()


    val locationPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        LocationPermission(context)
    if (!locationPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            composeProgressStatus.value = false
            locationPermissionStatus.second.launchMultiplePermissionRequest()
        }
    } else {
        val bluetoothPermissionStatus = BluetoothPermission(context)
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
                //bluetoothScanService = BluetoothScanService(context)


                var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
                val mapMarkerState = rememberMarkerState(geoPoint = GeoPoint(0.0, 0.0))
                var mapProperties by remember { mutableStateOf(DefaultMapProperties) }
                mapProperties = mapProperties.copy(isTilesScaledToDpi = true)
                    .copy(tileSources = TileSourceFactory.MAPNIK)
                    .copy(isEnableRotationGesture = false)
                    .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER)

                val cameraState = rememberCameraState {
                    geoPoint = GeoPoint(0.0, 0.0)
                    zoom = 19.0 // optional, default is 5.0
                }


                val networkLocation = AppFusedLocationRepo(context)
                LaunchedEffect(Unit) {
                    withTimeoutOrNull(10001) {
                        networkLocation.startCurrentLocation().flowOn(Dispatchers.IO)
                            .collect { loc ->
                                if (loc != null) {
                                    currentLocation = loc
                                    Log.v(
                                        "MainActivity", "LatLng : ${gson.toJson(currentLocation)}"
                                    )
                                    val geopoint = GeoPoint(
                                        currentLocation.latitude, currentLocation.longitude
                                    )
                                    cameraState.geoPoint = geopoint
                                    mapMarkerState.geoPoint = geopoint
                                }
                            }
                    }
                }


                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                val tDevice = dummyDevice

                val deviceFormHelper = DeviceFormHelper()
                val deviceIconlist: MutableList<FormDeviceItem>
                deviceIconlist = deviceFormHelper.FormDeviceList()

                val (selectedOption, onOptionSelected) = remember { mutableStateOf(deviceIconlist[0]) }

                var deviceName by rememberSaveable { mutableStateOf(tDevice.name) }
                var deviceMacaddress by rememberSaveable { mutableStateOf(tDevice.macaddress) }

                val deviceFormScaffoldState = rememberBottomSheetScaffoldState()
                //var devicelist = remember { mutableListOf<Device>() }
                val devicelistFlowState =
                    userviewModel.getDeviceFlowList()
                        .collectAsStateWithLifecycle(initialValue = mutableListOf())

                composeProgressStatus.value = false
                //end::Device Form assets
                Scaffold(bottomBar = {
                    BottomAppBar(
                        actions = {
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
                            FloatingActionButton(shape = CircleShape,
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                onClick = {
                                    scope.launch {
                                        composeProgressStatus.value = true
                                        delay(300)
                                        deviceFormScaffoldState.bottomSheetState.expand()
                                        if (deviceFormScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                            composeProgressStatus.value = false
                                        }
                                    }
                                },
                                content = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_add_24),
                                        contentDescription = ""
                                    )
                                })
                        },
                    )
                }) {
                    Column(modifier = Modifier.padding(it)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {

                            OpenStreetMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                cameraState = cameraState,
                                properties = mapProperties, // add properties
                            ) { Marker(state = mapMarkerState) }/*Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "Latitude: ${currentLocation.latitude} - ${currentLocation.longitude}")
                    }*/
                        }
                        HorizontalDivider(thickness = 3.dp, modifier = Modifier.fillMaxWidth())
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            itemsIndexed(devicelistFlowState.value) { _, dd ->
                                val deviceDismissShow by remember { mutableStateOf(true) }
                                val deviceDismissState = rememberSwipeToDismissState()
                                /* rememberDismissState(confirmValueChange = { dismissValue ->
                                     when (dismissValue) {
                                         DismissValue.DismissedToStart -> {
                                             userviewModel.DeleteDevice(dd)
                                             deviceDismissShow = false
                                             true
                                         }

                                         DismissValue.DismissedToEnd -> {
                                             scope.launch {
                                                 composeProgressStatus.value = true
                                                 delay(300)
                                                 deviceFormScaffoldState.bottomSheetState.expand()
                                             }
                                             deviceDismissShow = false
                                             true
                                         }

                                         else -> false
                                     }

                                 }, positionalThreshold = { 150f })*/
                                AnimatedVisibility(deviceDismissShow, exit = fadeOut(spring())) {
                                    SwipeToDismissBox(
                                        enableDismissFromStartToEnd = false,
                                        enableDismissFromEndToStart = true,
                                        state = deviceDismissState,
                                        backgroundContent = {
                                            val color by animateColorAsState(
                                                when (deviceDismissState.targetValue) {
                                                    SwipeToDismissValue.Settled -> Color.LightGray
                                                    SwipeToDismissValue.StartToEnd -> Color.Green
                                                    SwipeToDismissValue.EndToStart -> Color.Red
                                                }, label = ""
                                            )
                                            val eventIcon = when (deviceDismissState.targetValue) {
                                                SwipeToDismissValue.EndToStart -> R.drawable.baseline_delete_24
                                                else -> R.drawable.baseline_edit_24
                                            }
                                            val boxIconScale by animateFloatAsState(
                                                targetValue = if (deviceDismissState.targetValue == SwipeToDismissValue.Settled) .8f else 1.2f,
                                                label = ""
                                            )
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(color),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = eventIcon),
                                                    contentDescription = "",
                                                    modifier = Modifier
                                                        .scale(boxIconScale)
                                                        .padding(start = 24.dp, end = 24.dp)
                                                )
                                            }
                                        }) {
                                        Card(
                                            modifier = Modifier.clickable(onClick = {
                                                AppFusedLocationRepo.locationState.value =
                                                    AppFusedLocationState.STOP_LOCATION
                                                navController.navigate("devicedashboard/${dd.macaddress}")
                                            }),
                                            shape = RoundedCornerShape(0.dp),
                                        ) {
                                            ListItem(
                                                leadingContent = {
                                                    var deviceIcon = R.drawable.t3_icon_32
                                                    if (dd.devicetype == 2) deviceIcon =
                                                        R.drawable.e9_icon_32
                                                    Icon(
                                                        painter = painterResource(id = deviceIcon),
                                                        contentDescription = null,
                                                        tint = Color.DarkGray
                                                    )
                                                }, headlineContent = {
                                                    Text(
                                                        text = dd.name.uppercase(Locale.ROOT),
                                                        style = TextStyle(
                                                            color = Color.DarkGray,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 16.sp
                                                        )
                                                    )
                                                }, supportingContent = {
                                                    Text(
                                                        text = dd.macaddress.uppercase(Locale.ROOT),
                                                        style = TextStyle(
                                                            color = Color.Gray,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                    )
                                                },
                                                trailingContent = {
                                                    if (dd.istracking != null && dd.istracking == true) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_settings_input_antenna_24),
                                                            contentDescription = null,
                                                            tint = Color.Green
                                                        )
                                                    }
                                                    if(dd.ismissing!=null && dd.ismissing==true){
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_cell_tower_24),
                                                            contentDescription = null,
                                                            tint=Color.Red
                                                        )
                                                    }
                                                })
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //start::Device Form


                BottomSheetScaffold(scaffoldState = deviceFormScaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = context.getString(R.string.deviceformTitle),
                                style = formTitle,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(thickness = 1.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            TextField(
                                value = deviceMacaddress,
                                onValueChange = { deviceMacaddress = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(context.getString(R.string.macaddress)) },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    autoCorrect = false
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            TextField(
                                value = deviceName,
                                onValueChange = { deviceName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(context.getString(R.string.name)) },
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Column(Modifier.selectableGroup()) {
                                deviceIconlist.forEach {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .selectable(
                                                selected = (it == selectedOption),
                                                onClick = { onOptionSelected(it) },
                                                role = Role.RadioButton
                                            )
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (it == selectedOption),
                                            onClick = null // null recommended for accessibility with screenreaders
                                        )
                                        Text(
                                            text = context.getString(it.name),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                        Icon(
                                            painter = painterResource(id = it.image),
                                            modifier = Modifier.padding(start = 16.dp),
                                            contentDescription = context.getString(it.name)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row {
                                val obsAddEdit =
                                    userviewModel.mutablelivedataRMEventResult.collectAsState()
                                Button(onClick = {
                                    deviceName = ""
                                    deviceMacaddress = ""
                                    keyboardController!!.hide()
                                    scope.launch {
                                        deviceFormScaffoldState.bottomSheetState.partialExpand()
                                    }
                                }, content = { Text(context.getString(R.string.cancel)) })
                                Button(onClick = {
                                    scope.launch {
                                        composeProgressStatus.value = true
                                        keyboardController!!.hide()
                                        tDevice.name = deviceName
                                        tDevice.macaddress =
                                            helper.formatedMacAddress(deviceMacaddress)
                                        tDevice.devicetype = selectedOption.id
                                        tDevice.latitude = currentLocation.latitude.toString()
                                        tDevice.longitude = currentLocation.longitude.toString()
                                        tDevice.registerdate = helper.getNOWasString()

                                        userviewModel.addUpdateDevice(tDevice)
                                            .flowOn(Dispatchers.Default).cancellable()
                                            .collect { result ->
                                                when (result) {
                                                    null -> {
                                                        doNothing()
                                                    }

                                                    else -> {
                                                        if (result.resultFlag.flag == 1) {
                                                            scope.launch {
                                                                devicelistFlowState.value.add(
                                                                    tDevice
                                                                )
                                                                delay(600)
                                                                composeProgressStatus.value = false
                                                                //delay(400)
                                                                deviceFormScaffoldState.bottomSheetState.partialExpand()
                                                            }
                                                        } else {
                                                            if (result.error != null) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error Code : ${result.error!!.code}, Exception : ${result.error!!.exception}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        when (obsAddEdit.value.stateStatus) {
                                            RMEventStatus.Complete -> {
                                                deviceName = ""
                                                deviceMacaddress = ""

                                                scope.launch {
                                                    if (userviewModel.devicelist.add(tDevice)) {
                                                        composeProgressStatus.value = false
                                                    }
                                                    delay(400)
                                                    deviceFormScaffoldState.bottomSheetState.partialExpand()
                                                }
                                            }

                                            RMEventStatus.Exception -> {
                                                composeProgressStatus.value = false
                                                val formEventRes = obsAddEdit.value.formEventResult
                                                if (formEventRes != null) {
                                                    if (formEventRes.error != null) {
                                                        Toast.makeText(
                                                            context,
                                                            formEventRes.error!!.exception,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }

                                            else -> {
                                                composeProgressStatus.value = true
                                            }
                                        }
                                    }
                                }, content = { Text(context.getString(R.string.save)) })
                            }

                        }
                    }) {}
                //end:Device Form
            }
        }
    }
}