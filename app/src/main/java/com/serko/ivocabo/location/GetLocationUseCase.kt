package com.serko.ivocabo.location

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(private val locationService: ILocationService) {
    operator fun invoke(): Flow<LatLng?> = locationService.requestLocaltionUpdates()
    fun stop()=locationService.stopLocationUpdates()
}