package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


interface IAppFusedLocation {
    fun startCurrentLocation(): Flow<LatLng>
}

class AppFusedLocationRepo @Inject constructor(@ApplicationContext private val context: Context) :
    IAppFusedLocation {

    var stopLocationJob = MutableStateFlow<Boolean>(false)

    val gson = Gson()

    private lateinit var locationCallback: LocationCallback
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)


    @SuppressLint("MissingPermission")
    override fun startCurrentLocation(): Flow<LatLng> = callbackFlow {
        val locationRequest = LocationRequest
            .Builder(10000)
            .setIntervalMillis(10000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0 ?: return
                trySend(
                    LatLng(
                        p0.lastLocation!!.latitude,
                        p0.lastLocation!!.longitude
                    )
                ).isSuccess
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        stopLocationJob.collect {
            if (it)
                fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
    }.flowOn(Dispatchers.Default)
}

class GPSLocation @Inject constructor(@ApplicationContext private var context: Context) {
    var latLng = mutableStateOf(LatLng(0.0, 0.0))
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            5f,
            locationListener
        )

        /*MainScope().launch {
            delay(5200)
            locationManager.removeUpdates(locationListener)
        }*/

    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            latLng.value = LatLng(p0.latitude, p0.longitude)
            locationManager.removeUpdates(this)
        }
    }


}
