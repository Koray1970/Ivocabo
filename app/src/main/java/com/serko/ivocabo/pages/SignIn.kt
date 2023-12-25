package com.serko.ivocabo.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.google.gson.Gson
import com.serko.ivocabo.Helper
import com.serko.ivocabo.LocationPermission
import com.serko.ivocabo.R
import com.serko.ivocabo.SignUpFormHelper
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.User
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.remote.membership.SignInRequest
import com.serko.ivocabo.remote.membership.SignInResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignIn(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()
    val gson = Gson()
    val helper = Helper()
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
                if (!user.token.isNullOrEmpty()) navController.navigate(Screen.Dashboard.route)
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
                        Row {
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
                                if (IApiService.apiService == null) IApiService.getInstance()
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
                                                    when (rmResult.eventResult.error?.code) {
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
                                    ) {/*result.eventResult.error = Error(
                                        "SRV_SNG10",
                                        t.message.toString(),
                                        "com.serko.ivocabo.api.SrvMembership.invokeSignInService.call!!.enqueue.onFailure"
                                    )*/
                                    }
                                })
                            } catch (ex: Exception) {
                                Toast.makeText(
                                    context, "Exception : ${ex.localizedMessage}", Toast.LENGTH_LONG
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