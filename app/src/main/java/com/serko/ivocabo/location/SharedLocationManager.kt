package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class NetworkLocation constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<MutableState<LatLng>> = callbackFlow {
        val locationRequest = LocationRequest.Builder(1000)
            .setIntervalMillis(1000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                MainScope().launch {
                    send(
                        mutableStateOf(
                            LatLng(
                                p0.locations.last().latitude,
                                p0.locations.last().longitude
                            )
                        )
                    )
                }

                //fusedLocationClient.removeLocationUpdates(this)
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
    }

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
