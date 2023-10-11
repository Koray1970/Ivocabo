package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
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
import javax.inject.Inject

/*interface IGetCurrentLocation {
    fun getCurrentLocation(): MutableLiveData<LatLng?>
}

class GetCurrentLocation @Inject constructor(@ApplicationContext private val context: Context,val lifecycleOwner: LifecycleOwner) :
    IGetCurrentLocation {
    val checkInternetConnection = CheckInternetConnection(context)
    override fun getCurrentLocation(): MutableLiveData<LatLng?> {
        try {
            val internetConnectionState = checkInternetConnection.checkForInternet()
            if (internetConnectionState) {
                //get location from internet
                val networkLocation = NetworkLocation(context)
                networkLocation.getCurrentLocation()
                networkLocation.latLng.observe(lifecycleOwner){
                    when (it) {
                        null -> {DoNothing()}
                        else -> {
                            return@observe MutableLiveData(networkLocation.latLng.value)
                        }
                    }
                }

            } else {
                //get location from GPS
                val gpsLocation = GPSLocation(context)
                gpsLocation.getCurrentLocation()
                when (gpsLocation.latLng.value) {
                    null -> {DoNothing()}
                    else -> {
                        return MutableLiveData(gpsLocation.latLng.value)
                    }
                }
            }
        } catch (e: Exception) {

        }
        return MutableLiveData(null)
    }
}*/
fun DoNothing(){}

class NetworkLocation constructor(private val context: Context) {
    var latLng =MutableLiveData<LatLng?>(null)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation() {
        val locationRequest = LocationRequest.Builder(5000)
            .setIntervalMillis(5000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val locco = p0.locations.last()
            latLng.value = LatLng(locco.latitude, locco.longitude)
            fusedLocationClient.removeLocationUpdates(this)
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
            locationManager.removeUpdates(this@GetCurrentLocation)
        }*/
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            latLng.value = LatLng(p0.latitude, p0.longitude)
            locationManager.removeUpdates(this)
        }
    }


}