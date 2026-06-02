package com.partsystem.partvisitapp.core.utils.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHelper @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    // محاسبه فاصله به متر
    fun calculateDistance(
        userLat: Double, userLon: Double,
        targetLat: Double, targetLon: Double
    ): Float {
        val userLoc = Location("").apply { latitude = userLat; longitude = userLon }
        val targetLoc = Location("").apply { latitude = targetLat; longitude = targetLon }
        return userLoc.distanceTo(targetLoc)
    }
}
