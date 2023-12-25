package com.serko.ivocabo.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.serko.ivocabo.Helper
import com.serko.ivocabo.LocationPermission
import com.serko.ivocabo.R
import com.serko.ivocabo.Security
import com.serko.ivocabo.SignUpFormHelper
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.User
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.SignUpRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Signup(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()
    val helper = Helper()
    val scope = rememberCoroutineScope()

    var privacyIsDisplayed by remember { mutableStateOf(false) }
    var privacyBottomSheet by remember { mutableStateOf(false) }
    val privacysheetState = rememberModalBottomSheetState()
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
            if (!user.token.isNullOrEmpty()) navController.navigate(Screen.Dashboard.route)
            else navController.navigate(Screen.Signin.route)

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
                AlertDialog(onDismissRequest = { remoteResultOpenDialog = false },
                    confirmButton = {
                        remoteResultOpenDialog = false
                        navController.navigate(Screen.Signin.route)
                    },
                    title = { Text(text = context.getString(R.string.rgResultDialogTitle)) },
                    text = { Text(text = context.getString(R.string.rgResultDialogContent)) })
            }
            Scaffold(modifier = Modifier.padding(horizontal = 20.dp),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerpadding ->
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
                            Row {
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
                                                emailVal, passwordVal, usernameVal
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
                            Button(modifier = Modifier.fillMaxWidth(),
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