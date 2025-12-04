package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.entity.FinalFactorRequest
import com.partsystem.partvisitapp.core.network.ApiService
import retrofit2.Response
import javax.inject.Inject


class FactorRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun sendFactor(request: FinalFactorRequest): Response<Any> {
        return api.sendFactor(request)
    }
}