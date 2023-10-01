package com.serko.ivocabo.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val getLocationUseCase: GetLocationUseCase
):ViewModel() {
    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    fun stopLocation(){
        getLocationUseCase.stop()
    }
    fun startLocation(){
        viewModelScope.launch {
            getLocationUseCase.invoke().collect {
                _viewState.value = ViewState.Success(it)
            }
        }
    }
    fun handle(event: PermissionEvent) {
        when (event) {
            PermissionEvent.Granted -> {
                viewModelScope.launch {
                    getLocationUseCase.invoke().collect {
                        _viewState.value = ViewState.Success(it)

                    }
                }
            }

            /*PermissionEvent.Revoked -> {

            }*/

            else -> {_viewState.value = ViewState.RevokedPermissions}
        }
    }
}
sealed interface ViewState {
    object Loading : ViewState
    data class Success(val location: LatLng?) : ViewState
    object RevokedPermissions : ViewState
}

sealed interface PermissionEvent {
    object Granted : PermissionEvent
    object Revoked : PermissionEvent
}