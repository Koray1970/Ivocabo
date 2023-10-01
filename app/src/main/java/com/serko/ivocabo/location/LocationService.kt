package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.serko.ivocabo.hasLocationPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface ILocationService {
    fun requestLocaltionUpdates(): Flow<LatLng?>
    fun stopLocationUpdates()
}

class LocationService @Inject constructor(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient,

    ) : ILocationService {
    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun requestLocaltionUpdates(): Flow<LatLng?> = callbackFlow {
        if (!context.hasLocationPermission()) {
            trySend(null)
            return@callbackFlow
        }
        val request = LocationRequest.Builder(10000L)
            .setIntervalMillis(10000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let {
                    trySend(LatLng(it.latitude, it.longitude))
                    Log.i("locationExample", "onLocationResult: $it")
                }
            }
        }
        Log.v("LocationService", "Start")
        locationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
        awaitClose {
            locationClient.removeLocationUpdates(locationCallback!!)
        }
    }

    override fun stopLocationUpdates() {
        Log.v("LocationService", "Stop")
        if (locationCallback != null)
            locationClient.removeLocationUpdates(locationCallback!!)
    }
}