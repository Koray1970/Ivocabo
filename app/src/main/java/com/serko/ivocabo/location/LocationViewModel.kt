package com.serko.ivocabo.location

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILocationRepository {
    suspend fun GetCurrentLocation()
    suspend fun StopLocationUpdate()
}

class LocationRepository @Inject constructor(@ApplicationContext private val context: Context) :
    ILocationRepository {
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var latlang = MutableStateFlow(LatLng(0.0, 0.0))

    init {
        MainScope().launch {
            GetCurrentLocation()
        }
    }

    override suspend fun GetCurrentLocation() {
        try {
            locationProvider = LocationServices.getFusedLocationProviderClient(context)
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    if (p0.lastLocation != null) {
                        MainScope().launch {
                            latlang.emit(
                                LatLng(
                                    p0.lastLocation!!.latitude,
                                    p0.lastLocation!!.longitude
                                )
                            )
                        }
                    }
                    super.onLocationResult(p0)
                }
            }
        } catch (e: Exception) {

        }
    }

    override suspend fun StopLocationUpdate() {
        try {
            locationProvider.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {

        }
    }
}

@HiltViewModel
class LocationViewModel @Inject constructor(private val locRepo: LocationRepository) : ViewModel() {
    var latlang = MutableStateFlow(LatLng(0.0, 0.0))
    init {
        getCurrentLocation()
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            locRepo.GetCurrentLocation()
            latlang.emit(locRepo.latlang.value)
        }
    }

    fun stopLocationUpdate() {
        viewModelScope.launch {
            locRepo.StopLocationUpdate()
        }
    }
}