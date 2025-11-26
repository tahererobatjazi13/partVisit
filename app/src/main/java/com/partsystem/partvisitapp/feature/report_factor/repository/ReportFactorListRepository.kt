package com.partsystem.partvisitapp.feature.report_factor.repository

import android.content.Context
import android.util.Log
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.mapper.toEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class ReportFactorListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService
) {
    /**
     * دریافت لیست سفارشات از API
     */
    suspend fun getReportFactorVisitor(type: Int, visitorId: Int): NetworkResult<List<ReportFactorDto>> {
        try {
            val response = api.getReportFactorVisitor(type, visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            return NetworkResult.Success(body)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

/*
    suspend fun getReportFactorVisitor(type: Int, visitorId: Int): NetworkResult<List<ReportFactorDto>> {
        return try {
            val response = api.getReportFactorVisitor(type, visitorId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }
*/

 /*   *//**
     * دریافت جزییات یک سفارش از API
     *//*
    suspend fun getOrderDetail(id: Int): NetworkResult<OrderDetail> {
        return try {
            val response = api.getOrderDetail(id)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }
*/
}




