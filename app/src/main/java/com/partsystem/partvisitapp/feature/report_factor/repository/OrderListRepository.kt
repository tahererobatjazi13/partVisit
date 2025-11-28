package com.partsystem.partvisitapp.feature.report_factor.repository

import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import retrofit2.Response
import javax.inject.Inject


class OrderListRepository @Inject constructor(
    private val api: ApiService
) {
    /**
     * دریافت لیست از API
     */
    suspend fun getReportFactorVisitor(type: Int, visitorId: Int) =
        safeCall { api.getReportFactorVisitor(type, visitorId) }

    suspend fun getReportFactorDetail(type: Int, factorId: Int) =
        safeCall { api.getReportFactorDetail(type, factorId) }

    suspend fun getReportFactorCustomer(type: Int, customerId: Int) =
        safeCall { api.getReportFactorCustomer(type, customerId) }

    private suspend fun safeCall(block: suspend () -> Response<List<ReportFactorDto>>)
            : NetworkResult<List<ReportFactorDto>> {

        return try {
            val response = block()
            val body = response.body()

            if (response.isSuccessful && body != null)
                NetworkResult.Success(body)
            else
                NetworkResult.Error("Server Error: ${response.code()}")

        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }
}




