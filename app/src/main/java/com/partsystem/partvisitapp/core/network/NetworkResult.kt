package com.partsystem.partvisitapp.core.network

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T, val message: String? = null) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}