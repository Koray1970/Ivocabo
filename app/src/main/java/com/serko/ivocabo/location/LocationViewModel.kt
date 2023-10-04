package com.serko.ivocabo.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILocationRepository {
    fun GetCurrentLocation(): Flow<LocatationServiceResult>
    fun StopLocationUpdate()
}

class LocationRepository @Inject constructor(@ApplicationContext private val context: Context) :
    ILocationRepository {
    private lateinit var locationProvider: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    var latlang = MutableLiveData<LatLng>()

    init {
        MainScope().launch {
            GetCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    override fun GetCurrentLocation(): Flow<LocatationServiceResult> {
        var evresult = LocatationServiceResult()

        return callbackFlow<LocatationServiceResult> {
            try {
                locationProvider = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        p0.locations.forEach {
                            MainScope().launch {
                                evresult.statestatus = LOCATIONSTATUS.Running
                                evresult.latlng = LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                                send(evresult)

                            }
                        }
                        super.onLocationResult(p0)
                    }
                }
                locationProvider?.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    Looper.getMainLooper()
                )

            } catch (e: Exception) {
                MainScope().launch {
                    evresult.statestatus = LOCATIONSTATUS.Has_Exception
                    send(evresult)
                }
                Log.v("Location Detail", "Exception :${e.message}")
            }
            awaitCancellation()
            awaitClose {
                StopLocationUpdate()
                MainScope().launch {
                    evresult.statestatus = LOCATIONSTATUS.Stopped
                    send(evresult)
                }
            }
        }.flowOn(Dispatchers.IO)

    }

    override fun StopLocationUpdate() {
        try {
            locationCallback?.let {
               var lll:Task<Void> = locationProvider.removeLocationUpdates(it)
                if(lll.isSuccessful)
                    Log.v("Location Detail", "Location Update Stop")
                else
                    Log.v("Location Detail", "Location Update still running!!!")

            }
        } catch (e: Exception) {
            Log.v("Location Detail", "Stop Location Exception :${e.message}")
        }
    }
}

@HiltViewModel
class LocationViewModel @Inject constructor(private val locRepo: LocationRepository) : ViewModel() {
    private var _latlang = flowOf<LocatationServiceResult>()
    val latlang: Flow<LocatationServiceResult>
        get() = locRepo.GetCurrentLocation()


    /*init {
        viewModelScope.launch {
            _latlang =
        }
    }*/

    fun stopLocationUpdate() {
        viewModelScope.launch {
            locRepo.StopLocationUpdate()
        }
    }
}

enum class LOCATIONSTATUS { Initial, Running, Stopped, Has_Exception }
class LocatationServiceResult {
    var statestatus: LOCATIONSTATUS = LOCATIONSTATUS.Initial
    var latlng: LatLng = LatLng(0.0, 0.0)
}