package com.serko.ivocabo

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue.*
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.bluetooth.BluetoothScanService
import com.serko.ivocabo.data.Device
import com.serko.ivocabo.data.RMEventStatus
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.User
import com.serko.ivocabo.data.userViewModel
import com.serko.ivocabo.location.AppFusedLocationRepo
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.SignInRequest
import com.serko.ivocabo.remote.membership.SignInResponse
import com.serko.ivocabo.remote.membership.SignUpRequest
import com.serko.ivocabo.ui.theme.IvocaboTheme
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

//koko
//koko@gmail.com
//123456
@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    //private val userviewModel: userViewModel by viewModels()


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
        setContent {
            IvocaboTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val composeProgressDialogStatus = remember { mutableStateOf(false) }
                    AppNavigation(composeProgressDialogStatus)

                    ComposeProgress(composeProgressDialogStatus)
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

val gson = Gson()
val helper = Helper()

private val dummyDevice = Device(null, "", null, "", null, null, null, null, null, null)
private lateinit var bluetoothScanService: BluetoothScanService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeProgress(dialogshow: MutableState<Boolean>) {
    if (dialogshow.value) {
        AlertDialog(
            onDismissRequest = { dialogshow.value = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            content = {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = .7f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(100.dp),
                            strokeWidth = 16.dp,
                            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
                        )
                    }
                }
            })
    }
}

val formTitle =
    TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

fun DoNothing() {}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Dashboard(
    navController: NavController = rememberNavController(),
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
) {
    composeProgressStatus.value = true
    val context = LocalContext.current.applicationContext

    /*var user = userviewModel.fetchUser()
    var tokenData: Data? = null*/


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
                bluetoothScanService = BluetoothScanService(context)
                var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
                val mapMarkerState = rememberMarkerState(geoPoint = GeoPoint(0.0, 0.0))
                var mapProperties by remember { mutableStateOf(DefaultMapProperties) }
                mapProperties = mapProperties
                    .copy(isTilesScaledToDpi = true)
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
                        networkLocation.startCurrentLocation()
                            .flowOn(Dispatchers.IO)
                            .collect { loc ->
                                if (loc != null) {
                                    currentLocation = loc
                                    Log.v(
                                        "MainActivity",
                                        "LatLng : ${gson.toJson(currentLocation)}"
                                    )
                                    val geopoint =
                                        GeoPoint(
                                            currentLocation.latitude,
                                            currentLocation.longitude
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
                deviceIconlist = deviceFormHelper.FormDeviceList(context)

                val (selectedOption, onOptionSelected) = remember { mutableStateOf(deviceIconlist[0]) }

                var deviceName by rememberSaveable { mutableStateOf(tDevice.name) }
                var deviceMacaddress by rememberSaveable { mutableStateOf(tDevice.macaddress) }

                val deviceFormScaffoldState = rememberBottomSheetScaffoldState()
                //var devicelist = remember { mutableListOf<Device>() }
                var devicelistFlowState =
                    userviewModel.getDeviceFlowList()
                        .collectAsState(initial = mutableListOf<Device>())

                composeProgressStatus.value = false
                //end::Device Form assets
                Scaffold(
                    bottomBar = {
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
                                FloatingActionButton(
                                    shape = CircleShape,
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
                                    }
                                )
                            },
                        )
                    }
                ) {
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
                            ) { Marker(state = mapMarkerState) }
                            /*Box(
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
                                var deviceDismissShow by remember { mutableStateOf(true) }
                                val deviceDismissState =
                                    rememberDismissState(confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            DismissedToStart -> {
                                                userviewModel.DeleteDevice(dd)
                                                deviceDismissShow = false
                                                true
                                            }

                                            DismissedToEnd -> {
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

                                    }, positionalThreshold = { 150f })
                                AnimatedVisibility(deviceDismissShow, exit = fadeOut(spring())) {
                                    SwipeToDismiss(
                                        state = deviceDismissState,
                                        directions = setOf(DismissDirection.EndToStart),
                                        background = {
                                            val direction =
                                                deviceDismissState.dismissDirection
                                                    ?: return@SwipeToDismiss
                                            val color by animateColorAsState(
                                                when (deviceDismissState.targetValue) {
                                                    Default -> Color.LightGray
                                                    DismissedToEnd -> Color.Green
                                                    else -> Color.Red
                                                },
                                                label = ""
                                            )
                                            val eventIcon = when (direction) {
                                                DismissDirection.StartToEnd -> R.drawable.baseline_edit_24
                                                DismissDirection.EndToStart -> R.drawable.baseline_delete_24
                                            }
                                            val boxIconScale by animateFloatAsState(
                                                targetValue =
                                                if (deviceDismissState.targetValue == Default)
                                                    .8f else 1.2f, label = ""
                                            )
                                            val boxAlignment =
                                                when (direction) {
                                                    DismissDirection.StartToEnd -> Alignment.CenterStart
                                                    DismissDirection.EndToStart -> Alignment.CenterEnd
                                                }
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(color),
                                                contentAlignment = boxAlignment
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = eventIcon),
                                                    contentDescription = "",
                                                    modifier = Modifier
                                                        .scale(boxIconScale)
                                                        .padding(start = 24.dp, end = 24.dp)
                                                )
                                            }
                                        },
                                        dismissContent = {
                                            Card(
                                                modifier = Modifier.clickable(onClick = {
                                                    networkLocation.stopLocationJob.tryEmit(true)
                                                    navController.navigate("devicedashboard/${dd.macaddress}")
                                                }),
                                                shape = RoundedCornerShape(0.dp),
                                            ) {
                                                ListItem(
                                                    leadingContent = {
                                                        var deviceIcon = R.drawable.t3_icon_32
                                                        if (dd.devicetype == 2)
                                                            deviceIcon = R.drawable.e9_icon_32
                                                        Icon(
                                                            painter = painterResource(id = deviceIcon),
                                                            contentDescription = null,
                                                            tint = Color.DarkGray
                                                        )
                                                    },
                                                    headlineContent = {
                                                        Text(
                                                            text = dd.name.uppercase(Locale.ROOT),
                                                            style = TextStyle(
                                                                color = Color.DarkGray,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 16.sp
                                                            )
                                                        )
                                                    },
                                                    supportingContent = {
                                                        Text(
                                                            text = dd.macaddress.uppercase(Locale.ROOT),
                                                            style = TextStyle(
                                                                color = Color.Gray,
                                                                fontWeight = FontWeight.SemiBold,
                                                            )
                                                        )
                                                    }
                                                )
                                                HorizontalDivider()
                                            }
                                        })
                                }
                            }
                        }
                    }
                }
                //start::Device Form


                BottomSheetScaffold(
                    scaffoldState = deviceFormScaffoldState,
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
                                Button(
                                    onClick = {
                                        deviceName = ""
                                        deviceMacaddress = ""
                                        keyboardController!!.hide()
                                        scope.launch {
                                            deviceFormScaffoldState.bottomSheetState.partialExpand()
                                        }
                                    },
                                    content = { Text(context.getString(R.string.cancel)) }
                                )
                                Button(
                                    onClick = {
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
                                                            DoNothing()
                                                        }

                                                        else -> {
                                                            if (result.resultFlag.flag == 1) {
                                                                scope.launch {
                                                                    devicelistFlowState.value.add(
                                                                        tDevice
                                                                    )
                                                                    delay(600)
                                                                    composeProgressStatus.value =
                                                                        false
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
                                                    val formEventRes =
                                                        obsAddEdit.value.formEventResult
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
                                    },
                                    content = { Text(context.getString(R.string.save)) }
                                )
                            }

                        }
                    }
                ) {}
                //end:Device Form
            }
        }
    }
}


@Composable
fun ForgetPassword(navController: NavController) {

}

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignIn(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
) {
    val context = LocalContext.current.applicationContext
    val locationPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        LocationPermission(context)

    if (!locationPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            locationPermissionStatus.second.launchMultiplePermissionRequest()
        }
    } else {

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            val countofUser = userviewModel.getCountofUser()
            if (countofUser > 0) {
                val user = userviewModel.fetchUser()
                if (!user.token.isNullOrEmpty())
                    navController.navigate(Screen.Dashboard.route)
            }
        }


        val formHelper = SignUpFormHelper()

        var usernameVal by remember { mutableStateOf("") }
        var usernameError by remember { mutableStateOf(false) }
        val usernameLimit = 64
        var passwordVal by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(true) }
        var passwordError by remember { mutableStateOf(false) }
        val passwordLimit = 16
        val required = context.getString(R.string.required)
        val limit = context.getString(R.string.limit)
        Surface(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    modifier = Modifier.width(180.dp),
                    alignment = Alignment.Center,
                    painter = painterResource(id = R.drawable.ivocabo_logo_appicon),
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier.padding(vertical = 10.dp),
                    text = context.getString(R.string.signinTitle),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                TextField(value = usernameVal,
                    onValueChange = {
                        if (usernameVal.length < usernameLimit) usernameVal = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    placeholder = { Text(text = context.getString(R.string.username)) },
                    label = { Text(text = context.getString(R.string.username)) },
                    singleLine = true,
                    isError = usernameError,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = required, color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "$limit ${usernameVal.length}/$usernameLimit")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    trailingIcon = {
                        IconButton(onClick = { usernameVal = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_clear_24),
                                contentDescription = ""
                            )
                        }
                    })
                TextField(value = passwordVal,
                    onValueChange = {
                        passwordVal = it.take(passwordLimit)
                    },
                    placeholder = { Text(text = context.getString(R.string.password)) },
                    label = { Text(text = context.getString(R.string.password)) },
                    singleLine = true,
                    isError = passwordError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = required, color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "$limit ${passwordVal.length}/$passwordLimit")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        var iconResource = R.drawable.baseline_visibility_off_24
                        if (passwordVisible) iconResource = R.drawable.baseline_visibility_24
                        Row() {
                            IconButton(onClick = { passwordVal = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_clear_24),
                                    contentDescription = ""
                                )
                            }

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(iconResource), contentDescription = ""
                                )
                            }
                        }
                    })
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    Button(modifier = Modifier.padding(horizontal = 3.dp), onClick = {
                        composeProgressStatus.value = true
                        navController.navigate(Screen.Signup.route)
                    }) {
                        Text(text = context.getString(R.string.cancel))
                    }
                    Button(onClick = {
                        usernameError = formHelper.checkUsername(usernameVal)
                        passwordError = formHelper.checkPassword(passwordVal)
                        if (usernameError && passwordError) {
                            try {
                                if (IApiService.apiService == null)
                                    IApiService.getInstance()
                                val apiSrv = IApiService.apiService
                                val call: Call<SignInResponse>? = apiSrv?.srvSignIn(
                                    SignInRequest(
                                        passwordVal, usernameVal
                                    )
                                )
                                call!!.enqueue(object : Callback<SignInResponse> {
                                    override fun onResponse(
                                        call: Call<SignInResponse>,
                                        response: Response<SignInResponse>,
                                    ) {
                                        if (response.isSuccessful) {
                                            val rmResult = response.body()!!
                                            scope.launch {
                                                val countOfUser = userviewModel.getCountofUser()

                                                if (rmResult.eventResult.error != null) {
                                                    composeProgressStatus.value = false
                                                    when(rmResult.eventResult.error?.code) {
                                                        "SIN010" -> {
                                                            Toast.makeText(
                                                                context,
                                                                "User Not Found!",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }

                                                        else -> {
                                                            Toast.makeText(
                                                                context,
                                                                "Please try again!",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }

                                                } else {
                                                    if (countOfUser > 0) {
                                                        val user = userviewModel.fetchUser()
                                                        user.token = rmResult.token
                                                        userviewModel.updateUser(user)
                                                    } else {
                                                        val username = rmResult.username!!
                                                        val email = rmResult.email!!
                                                        userviewModel.insertUser(
                                                            User(
                                                                0,
                                                                helper.getNOWasSQLDate(),
                                                                username,
                                                                email,
                                                                rmResult.token,
                                                                if (rmResult.devicelist != null) {
                                                                    gson.toJson(rmResult.devicelist)
                                                                } else {
                                                                    null
                                                                }
                                                            )
                                                        )
                                                    }

                                                    composeProgressStatus.value = false
                                                    delay(120)
                                                    navController.navigate(Screen.Dashboard.route)
                                                }
                                            }
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<SignInResponse>,
                                        t: Throwable,
                                    ) {
                                        /*result.eventResult.error = Error(
                                        "SRV_SNG10",
                                        t.message.toString(),
                                        "com.serko.ivocabo.api.SrvMembership.invokeSignInService.call!!.enqueue.onFailure"
                                    )*/
                                    }
                                })
                            } catch (ex: Exception) {
                                Toast.makeText(
                                    context,
                                    "Exception : ${ex.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }) {
                        Text(text = context.getString(R.string.signin))
                    }
                }
            }

        }
    }
    LaunchedEffect(Unit) {
        delay(200)
        composeProgressStatus.value = false
    }
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Signup(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    var privacyIsDisplayed by remember { mutableStateOf(false) }
    var privacyBottomSheet by remember { mutableStateOf(false) }
    var privacysheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    val locationPermissionStatus: Pair<Boolean, MultiplePermissionsState> =
        LocationPermission(context)

    if (!locationPermissionStatus.first) {
        LaunchedEffect(Unit) {
            delay(300)
            locationPermissionStatus.second.launchMultiplePermissionRequest()
        }
    } else {
        composeProgressStatus.value = true
        val countOfUser = userviewModel.getCountofUser()
        if (countOfUser > 0) {
            val user = userviewModel.fetchUser()
            composeProgressStatus.value = false
            if (!user.token.isNullOrEmpty())
                navController.navigate(Screen.Dashboard.route)
            else
                navController.navigate(Screen.Signin.route)

        } else {
            val formHelper = SignUpFormHelper()
            val security = Security()


            var usernameVal by remember { mutableStateOf("") }
            var usernameError by remember { mutableStateOf(false) }
            val usernameLimit = 64

            var emailVal by remember { mutableStateOf("") }
            var emailError by remember { mutableStateOf(false) }
            val emailLimit = 320
            var passwordVal by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(true) }
            var passwordError by remember { mutableStateOf(false) }
            val passwordLimit = 16
            val required = context.getString(R.string.required)
            val limit = context.getString(R.string.limit)
            composeProgressStatus.value = false
            //remote result open dialog
            var remoteResultOpenDialog by remember { mutableStateOf(false) }

            if (remoteResultOpenDialog) {
                AlertDialog(
                    onDismissRequest = { remoteResultOpenDialog = false },
                    confirmButton = {
                        remoteResultOpenDialog = false
                        navController.navigate(Screen.Signin.route)
                    },
                    title = { Text(text = context.getString(R.string.rgResultDialogTitle)) },
                    text = { Text(text = context.getString(R.string.rgResultDialogContent)) }
                )
            }
            Scaffold(
                modifier = Modifier.padding(horizontal = 20.dp),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerpadding ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxWidth()
                ) {
                    Image(
                        modifier = Modifier.width(180.dp),
                        alignment = Alignment.Center,
                        painter = painterResource(id = R.drawable.ivocabo_logo_appicon),
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        text = context.getString(R.string.signupTitle),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(value = usernameVal,
                        onValueChange = {
                            if (usernameVal.length < usernameLimit) usernameVal = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        placeholder = { Text(text = context.getString(R.string.username)) },
                        label = { Text(text = context.getString(R.string.username)) },
                        singleLine = true,
                        isError = usernameError,
                        supportingText = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = required, color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "$limit ${usernameVal.length}/$usernameLimit")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        trailingIcon = {
                            IconButton(onClick = { usernameVal = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_clear_24),
                                    contentDescription = ""
                                )
                            }
                        })
                    TextField(value = emailVal,
                        onValueChange = {
                            if (emailVal.length < emailLimit) emailVal = it
                        },
                        placeholder = { Text(text = context.getString(R.string.email)) },
                        label = { Text(text = context.getString(R.string.email)) },
                        singleLine = true,
                        isError = emailError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        supportingText = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = required, color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "$limit ${emailVal.length}/$emailLimit")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email
                        ),
                        trailingIcon = {
                            IconButton(onClick = { emailVal = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_clear_24),
                                    contentDescription = ""
                                )
                            }
                        })
                    TextField(value = passwordVal,
                        onValueChange = {
                            passwordVal = it.take(passwordLimit)
                        },
                        placeholder = { Text(text = context.getString(R.string.password)) },
                        label = { Text(text = context.getString(R.string.password)) },
                        singleLine = true,
                        isError = passwordError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        supportingText = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = required, color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "$limit ${passwordVal.length}/$passwordLimit")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            var iconResource = R.drawable.baseline_visibility_off_24
                            if (passwordVisible) iconResource = R.drawable.baseline_visibility_24
                            Row() {
                                IconButton(onClick = { passwordVal = "" }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_clear_24),
                                        contentDescription = ""
                                    )
                                }
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(iconResource),
                                        contentDescription = ""
                                    )
                                }
                            }
                        })
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { privacyBottomSheet = true }) {
                            Text(text = context.getString(R.string.signupprivacypolicybutton))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(modifier = Modifier.padding(horizontal = 3.dp), onClick = {
                            composeProgressStatus.value = true
                            navController.navigate(Screen.Signin.route)
                        }) {
                            Text(text = context.getString(R.string.signin))
                        }
                        Spacer(modifier = Modifier.width(30.dp))
                        Button(onClick = {
                            if (!privacyIsDisplayed) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.signuppleasedisplayandreadprivacy))
                                }
                            } else {
                                composeProgressStatus.value = true
                                usernameError = formHelper.checkUsername(usernameVal)
                                emailError = formHelper.checkEmail(emailVal)
                                passwordError = formHelper.checkPassword(passwordVal)
                                if (!(usernameError && emailError && passwordError)) {
                                    try {
                                        //add to remote server

                                        IApiService.getInstance()
                                        val apiSrv = IApiService.apiService
                                        val call: Call<EventResult> = apiSrv!!.srvSignUp(
                                            SignUpRequest(
                                                emailVal,
                                                passwordVal,
                                                usernameVal
                                            )
                                        )
                                        call.enqueue(object : Callback<EventResult> {
                                            override fun onResponse(
                                                call: Call<EventResult>,
                                                response: Response<EventResult>,
                                            ) {
                                                if (response.isSuccessful) {
                                                    if (response.body()!!.eventresultflag == 0) {
                                                        //now u can add form items to database
                                                        val enUserVal =
                                                            security.encrypt(usernameVal)
                                                        val enEmailVal = security.encrypt(emailVal)

                                                        val user = User(
                                                            0,
                                                            helper.getNOWasSQLDate(),
                                                            enUserVal,
                                                            enEmailVal,
                                                            null,
                                                            null
                                                        )
                                                        scope.launch {
                                                            userviewModel.insertUser(user)
                                                            delay(300)
                                                            composeProgressStatus.value = false
                                                            remoteResultOpenDialog = true
                                                        }
                                                    } else {
                                                        if (response.body()!!.error != null) {
                                                            val eRror = response.body()!!.error!!
                                                            Toast.makeText(
                                                                context,
                                                                "Error Code : ${eRror.code}, Exception: ${eRror.exception}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                        composeProgressStatus.value = false
                                                    }
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<EventResult>,
                                                t: Throwable,
                                            ) {
                                                Toast.makeText(
                                                    context,
                                                    "Error Code : SRV_REGEX10, Message : ${t.message.toString()}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        })
                                    } catch (ex: Exception) {
                                        Toast.makeText(
                                            context,
                                            "General Exception: ${ex.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        composeProgressStatus.value = false
                                    }
                                } else {
                                    composeProgressStatus.value = false
                                }
                            }
                        }) {
                            Text(text = context.getString(R.string.save))
                        }
                    }
                }
                if (privacyBottomSheet) {
                    ModalBottomSheet(
                        shape = RectangleShape,
                        onDismissRequest = {
                            privacyIsDisplayed = true
                            privacyBottomSheet = false
                        },
                        containerColor = Color.White,
                        sheetState = privacysheetState,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                onClick = {
                                    scope.launch { privacysheetState.hide() }.invokeOnCompletion {
                                        if (!privacysheetState.isVisible) {
                                            privacyIsDisplayed = true
                                            privacyBottomSheet = false
                                        }
                                    }
                                }) {
                                Text(text = context.getString(R.string.ireadandunderstand))
                            }
                            PrivacyViewer(context.getString(R.string.privacypolicyurl))
                        }
                    }
                }
            }
            LaunchedEffect(Unit) {
                delay(200)
                composeProgressStatus.value = false
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeviceDashboard(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
    //locationViewModel: LocationViewModel = hiltViewModel()
) {
    composeProgressStatus.value = true
    val context = LocalContext.current.applicationContext
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
            var deviceDetail by remember { mutableStateOf<Device>(dummyDevice) }
            var chkNotificationCheckState by remember { mutableStateOf(false) }
            chkNotificationCheckState = deviceDetail.istracking ?: null == true
            var chkMissingCheckState by remember { mutableStateOf(false) }
            chkMissingCheckState = deviceDetail.ismissing ?: null == true
            val mapMarkerState = rememberMarkerState(geoPoint = GeoPoint(0.0, 0.0))
            var mapProperties by remember { mutableStateOf(DefaultMapProperties) }
            val cameraState = rememberCameraState {
                geoPoint = GeoPoint(0.0, 0.0)
                zoom = 19.0 // optional, default is 5.0
            }

            SideEffect {
                deviceDetail = userviewModel.getDeviceDetail(macaddress = macaddress!!)!!

                //start:Map Properties
                val geopoint =
                    GeoPoint(
                        deviceDetail.latitude!!.toDouble(),
                        deviceDetail.longitude!!.toDouble()
                    )
                cameraState.geoPoint = geopoint
                mapMarkerState.geoPoint = geopoint

                mapProperties = mapProperties
                    .copy(isTilesScaledToDpi = true)
                    .copy(tileSources = TileSourceFactory.MAPNIK)
                    .copy(isEnableRotationGesture = false)
                    .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER)
                scope.launch {
                    delay(320)
                    composeProgressStatus.value = false
                }
            }
            //end:Map Properties

            val workManager = WorkManager.getInstance(context)

            var trackWorkRequest = OneTimeWorkRequestBuilder<TrackWorker>()
                .setInputData(
                    Data.Builder().putString("macaddress", macaddress!!.uppercase(Locale.ROOT))
                        .build()
                )
                .build()


            Scaffold(
                bottomBar = {
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
                            FloatingActionButton(
                                containerColor = Color.Green,
                                shape = CircleShape,
                                onClick = {
                                    navController.navigate(Screen.Dashboard.route)
                                },
                                content = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                        contentDescription = ""
                                    )
                                }
                            )
                        },
                    )
                }
            ) { it ->
                Column(modifier = Modifier.padding(it)) {
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
                                text = deviceDetail.name,
                                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${deviceDetail.macaddress.uppercase(Locale.ROOT)} ${deviceDetail.registerdate}",
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Light),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                ElevatedButton(
                                    modifier = Modifier
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
                                        navController.navigate("trackmydevice/${deviceDetail.macaddress}")
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
                                        "\n${deviceDetail.name}"
                                    )
                                    Text(text = txttrack, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                ElevatedButton(
                                    modifier = Modifier
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
                                        navController.navigate("findmydevice/${deviceDetail.macaddress}")
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
                                        "\n${deviceDetail.name}"
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
                                    )
                                    {
                                        Switch(
                                            checked = chkNotificationCheckState,
                                            onCheckedChange = { cc ->


                                                //track switch onchecked
                                                MainScope().launch {
                                                    val getloc =
                                                        AppFusedLocationRepo(context).startCurrentLocation()
                                                            .cancellable().first()
                                                    delay(100)
                                                    if (!cc) {
                                                        workManager.cancelWorkById(trackWorkRequest.id)
                                                        deviceDetail.istracking = null
                                                    } else {
                                                        workManager.enqueue(trackWorkRequest)
                                                        deviceDetail.istracking = true
                                                        deviceDetail.ismissing = null
                                                    }
                                                    deviceDetail.longitude =
                                                        getloc.longitude.toString()
                                                    deviceDetail.latitude =
                                                        getloc.latitude.toString()
                                                    userviewModel.addUpdateDevice(deviceDetail)
                                                        .flowOn(Dispatchers.Default).cancellable()
                                                        .collect { result ->
                                                            when (result) {
                                                                null -> {
                                                                    DoNothing()
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
                                                                        } else
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Tracking servisi kapatıldı.",
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
                                            }
                                        )
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
                                    )
                                    {
                                        Switch(
                                            checked = chkMissingCheckState,
                                            onCheckedChange = {
                                                MainScope().launch {
                                                    val getloc =
                                                        AppFusedLocationRepo(context).startCurrentLocation()
                                                            .cancellable().first()
                                                    delay(100)
                                                    if (!it)
                                                        deviceDetail.ismissing = null
                                                    else {
                                                        deviceDetail.istracking = null
                                                        deviceDetail.ismissing = true
                                                    }
                                                    deviceDetail.longitude =
                                                        getloc.longitude.toString()
                                                    deviceDetail.latitude =
                                                        getloc.latitude.toString()
                                                    userviewModel.addUpdateDevice(deviceDetail)
                                                        .flowOn(Dispatchers.Default).cancellable()
                                                        .collect { result ->
                                                            when (result) {
                                                                null -> {
                                                                    DoNothing()
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
                                                                        } else
                                                                            Toast.makeText(
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
                                            },
                                            thumbContent = if (chkMissingCheckState) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                                    )
                                                }
                                            } else {
                                                null
                                            }
                                        )
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

val defaultMetricTextStyle = TextStyle(
    fontWeight = FontWeight.ExtraBold,
    fontSize = 48.sp,
    textAlign = TextAlign.Center
)
val scanningMetricTextStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    textAlign = TextAlign.Center,
    color = Color.Red
)
var metricDistance = mutableStateOf("")
var metricDistanceTextStyle = mutableStateOf<TextStyle>(defaultMetricTextStyle)

@Composable
fun ComposeScanResultUI(
    context: Context,
    macaddress: String,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    metricDistance.value = context.getString(R.string.scanning)
    var disconnectedCounter = 0
    if (bluetoothScanService == null) {
        bluetoothScanService = BluetoothScanService(context)
    }
    if (!bluetoothScanService.scanList.isNullOrEmpty()) {
        if (bluetoothScanService.scanList.none { a -> a.deviceAddress == macaddress }) {
            bluetoothScanService.scanList.add(
                ScanFilter.Builder().setDeviceAddress(macaddress).build()
            )
        }
    } else {
        bluetoothScanService.scanList.add(
            ScanFilter.Builder().setDeviceAddress(macaddress).build()
        )
    }
    bluetoothScanService!!.scanJonState.tryEmit(true)


    metricDistanceTextStyle.value = scanningMetricTextStyle
    metricDistance.value = context.getString(R.string.scanning)
    var cRssi by remember { mutableStateOf<Int?>(null) }
    /*val notify = AppNotification(
    context,
    NotifyItem(deviceDetail, "Title", "Summary", "Context"))*/

    LaunchedEffect(Unit) {
        bluetoothScanService.bluetoothScannerResults().flowOn(Dispatchers.Default).cancellable()
            .collect { rlt ->
                if (!rlt.isNullOrEmpty()) {
                    when (rlt.size) {
                        1 -> {
                            Log.v("MainActivity", "bluetoothScannerResults 2")
                            var dvScanResult = rlt.first { a -> a.macaddress == macaddress }
                            if (dvScanResult != null) {
                                disconnectedCounter = 0
                                metricDistanceTextStyle.value = defaultMetricTextStyle
                                if (composeProgressStatus.value)
                                    composeProgressStatus.value = false
                                cRssi = dvScanResult.rssi
                                metricDistance.value =
                                    helper.CalculateRSSIToMeter(cRssi)
                                        .toString() + "mt"
                                Log.v("MainActivity", "bluetoothScannerResults 2.1")
                            } else {
                                disconnectedCounter = disconnectedCounter + 1
                                Log.v("MainActivity", "bluetoothScannerResults 2.2")
                                if (disconnectedCounter >= 10) {
                                    cRssi = null
                                    metricDistanceTextStyle.value = scanningMetricTextStyle
                                    metricDistance.value =
                                        context.getString(R.string.devicecannotbereached)
                                    Log.v("MainActivity", "bluetoothScannerResults 2.3")
                                } else
                                    metricDistance.value =
                                        helper.CalculateRSSIToMeter(cRssi)
                                            .toString() + "mt"
                            }

                        }

                        else -> {

                            if (cRssi != null)
                                metricDistance.value =
                                    helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                            else
                                metricDistance.value = context.getString(R.string.scanning)
                            disconnectedCounter = disconnectedCounter + 1
                            Log.v("MainActivity", "bluetoothScannerResults 3")
                            if (disconnectedCounter >= 10) {
                                cRssi = null
                                metricDistanceTextStyle.value = scanningMetricTextStyle
                                metricDistance.value =
                                    context.getString(R.string.devicecannotbereached)
                                Log.v("MainActivity", "bluetoothScannerResults 3.1")
                            }
                        }
                    }
                } else {
                    Log.v("MainActivity", "bluetoothScannerResults 1")
                    if (cRssi != null)
                        metricDistance.value = helper.CalculateRSSIToMeter(cRssi).toString() + "mt"
                    else
                        metricDistance.value = context.getString(R.string.scanning)
                    disconnectedCounter = disconnectedCounter + 1
                    if (disconnectedCounter >= 10) {
                        cRssi = null
                        metricDistanceTextStyle.value = scanningMetricTextStyle
                        metricDistance.value =
                            context.getString(R.string.devicecannotbereached)
                        Log.v("MainActivity", "bluetoothScannerResults 1.1")
                    }
                }
            }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FindMyDevice(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
) {
    composeProgressStatus.value = true
    val context = LocalContext.current.applicationContext

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
        val scope = rememberCoroutineScope()

        var deviceDetail by remember { mutableStateOf(dummyDevice) }
        var deviceIcon = R.drawable.t3_icon_32


        deviceDetail = userviewModel.getDeviceDetail(macaddress = macaddress!!)!!
        if (deviceDetail.devicetype != null)
            if (deviceDetail.devicetype == 2)
                deviceIcon = R.drawable.e9_icon_32
        val _macaddress = macaddress.uppercase(Locale.ROOT)

        ComposeScanResultUI(
            context,
            _macaddress,
            composeProgressStatus
        )

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = Color.Green,
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
                    }
                )
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
                    text = context.getString(R.string.findmydevicetitle),
                    style = TextStyle(
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
        BackHandler(true) {}
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TrackMyDevice(
    macaddress: String?,
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel(),
) {

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
            if (deviceDetail.devicetype != null)
                if (deviceDetail.devicetype == 2)
                    deviceIcon = R.drawable.e9_icon_32
            val _macaddress = macaddress.uppercase(Locale.ROOT)
            ComposeScanResultUI(
                context = context,
                macaddress = _macaddress,
                composeProgressStatus = composeProgressStatus
            )

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        containerColor = Color.Green,
                        shape = CircleShape,
                        onClick = {
                            bluetoothScanService.scanJonState.tryEmit(true)
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
                        }
                    )
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
                        text = context.getString(R.string.trackmydevicetitle),
                        style = TextStyle(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val userDetail = userviewModel.fetchUser()

    var privacyBottomSheet by remember { mutableStateOf(false) }
    var privacysheetState = rememberModalBottomSheetState()
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Dashboard.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_home_24),
                            contentDescription = null
                        )
                    }
                    /*IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_24),
                            contentDescription = null
                        )
                    }*/
                    IconButton(onClick = { navController.navigate(Screen.Preference.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = null
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        containerColor = Color.Green,
                        shape = CircleShape,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route)
                        },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = ""
                            )
                        }
                    )
                },
            )
        }

    ) { innerpadding ->
        Column(
            modifier = Modifier
                .padding(innerpadding)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = context.getString(R.string.prf_title),
                style = ComposeTitle,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "${context.getString(R.string.username)} : ",
                style = profileFormLabel,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = userDetail.username,
                style = profileFormValue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${context.getString(R.string.email)} : ",
                style = profileFormLabel,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = userDetail.email,
                style = profileFormValue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { privacyBottomSheet = true },
                    shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_privacy_tip_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_privacypolicy))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row() {
                Button(
                    onClick = { /*TODO*/ },
                    shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_removeuser))
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        context.deleteDatabase(context.getString(R.string.dbname))
                        navController.navigate(Screen.Signin.route)
                    },
                    shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_signout))
                }
            }
        }

        if (privacyBottomSheet) {
            ModalBottomSheet(
                shape = RectangleShape,
                onDismissRequest = {
                    privacyBottomSheet = false
                },
                containerColor = Color.White,
                sheetState = privacysheetState,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        onClick = {
                            scope.launch { privacysheetState.hide() }.invokeOnCompletion {
                                if (!privacysheetState.isVisible) {
                                    privacyBottomSheet = false
                                }
                            }
                        }) {
                        Text(text = context.getString(R.string.ireadandunderstand))
                    }
                    PrivacyViewer(context.getString(R.string.privacypolicyurl))
                }
            }
        }
    }
}

@Composable
fun Preference(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel()
) {
    val context = LocalContext.current.applicationContext
    val userDetail = userviewModel.fetchUser()

    var locationPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        locationPermissionIcon = R.drawable.baseline_check_box_24

    var bluetoothPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        || context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    )
        bluetoothPermissionIcon = R.drawable.baseline_check_box_24

    var notificationPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        notificationPermissionIcon = R.drawable.baseline_check_box_24
    Scaffold(
        bottomBar = {
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
                    /*IconButton(onClick = { navController.navigate(Screen.Preference.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = null
                        )
                    }*/
                },
                floatingActionButton = {
                    FloatingActionButton(
                        containerColor = Color.Green,
                        shape = CircleShape,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route)
                        },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = ""
                            )
                        }
                    )
                },
            )
        }

    ) { innerpadding ->
        Column(
            modifier = Modifier
                .padding(innerpadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.pre_preferencetitle),
                style = ComposeTitle,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))

            ListItem(
                trailingContent = {
                    Icon(
                        painter = painterResource(id = locationPermissionIcon),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        text = "${context.getString(R.string.pre_locationpermission)} : ",
                        style = profileFormLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_location_on_24),
                        contentDescription = null
                    )
                },
                supportingContent = {
                    Text(
                        text = "${context.getString(R.string.pre_permissionratio)} : ",
                        style = preferanceSupportingText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
            HorizontalDivider()
            ListItem(
                trailingContent = {
                    Icon(
                        painter = painterResource(id = bluetoothPermissionIcon),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        text = "${context.getString(R.string.pre_bluetoothpermission)} : ",
                        style = profileFormLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_bluetooth_24),
                        contentDescription = null
                    )
                },
                supportingContent = {
                    Text(
                        text = "${context.getString(R.string.pre_permissionratio)} : ",
                        style = preferanceSupportingText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
            HorizontalDivider()
            ListItem(
                trailingContent = {
                    Icon(
                        painter = painterResource(id = notificationPermissionIcon),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        text = "${context.getString(R.string.pre_notificationpermission)} : ",
                        style = profileFormLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_notifications_24),
                        contentDescription = null
                    )
                },
                supportingContent = {
                    Text(
                        text = "${context.getString(R.string.pre_permissionratio)} : ",
                        style = preferanceSupportingText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
            HorizontalDivider()
            ListItem(
                trailingContent = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_info_24),
                            contentDescription = null
                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = "${context.getString(R.string.pre_privacypolicysubmitstamp)} : ",
                        style = profileFormLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_privacy_tip_24),
                        contentDescription = null
                    )
                },
                supportingContent = {
                    Text(
                        text = userDetail.registerdate.toString(),
                        style = preferanceSupportingText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun PrivacyViewer(url: String) = AndroidView(
    modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState()),
    factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }, update = { it.loadUrl(url) })


/*@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
IvocaboTheme {
Signup(navController)
}
}*/
