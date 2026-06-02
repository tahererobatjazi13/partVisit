package com.partsystem.partvisitapp.core.utils.location

sealed class LocationUiState {
    object Idle : LocationUiState()
    object CheckingPermission : LocationUiState()
    object CheckingLocationSettings : LocationUiState()
    object FetchingLocation : LocationUiState()
    object InsideStoreRange : LocationUiState()
    data class OutsideStoreRange(
        val distanceMeters: Float,
        val allowedRadiusMeters: Float
    ) : LocationUiState()
    data class Error(val message: String) : LocationUiState()
}
