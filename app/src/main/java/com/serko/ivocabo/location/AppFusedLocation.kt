package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.serko.ivocabo.hasLocationPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


interface IAppFusedLocation {
    fun startCurrentLocation(): Flow<LatLng?>
}

enum class AppFusedLocationState { INIT, START_LOCATION, STOP_LOCATION }
class AppFusedLocationRepo @Inject constructor(@ApplicationContext private val applicationContext: Context) :
    IAppFusedLocation {
    companion object {
        var locationState = mutableStateOf(AppFusedLocationState.INIT)
    }

    private lateinit var locationCallback: LocationCallback
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(applicationContext)

    @SuppressLint("MissingPermission")
    override fun startCurrentLocation(): Flow<LatLng> = callbackFlow {
        if (!applicationContext.hasLocationPermission())
            trySend(LatLng(0.0, 0.0))
        val locationRequest = LocationRequest
            .Builder(2000)
            .setIntervalMillis(2000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
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
        when (locationState.value) {
            AppFusedLocationState.STOP_LOCATION -> {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            AppFusedLocationState.INIT,
            AppFusedLocationState.START_LOCATION -> {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

    }.distinctUntilChanged()
}

class GPSLocation @Inject constructor(@ApplicationContext private var applicationContext: Context) {
    var latLng = mutableStateOf(LatLng(0.0, 0.0))
    private val locationManager =
        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
