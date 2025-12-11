package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.FinalFactorRequest
import com.partsystem.partvisitapp.core.network.ApiService
import retrofit2.Response
import javax.inject.Inject

class FactorRepository @Inject constructor(
    private val api: ApiService,
    private val factorDao: FactorDao,
) {
    suspend fun saveFactorHeader(factor: FactorHeaderEntity): Long {
        return factorDao.insertFactorHeader(factor)
    }

    suspend fun getAllFactors(): List<FactorHeaderEntity> = factorDao.getAllFactors()

    fun getAllFactorDetail() = factorDao.getAllFactorDetail()


    suspend fun deleteFactorDetail(productId: Int) =
        factorDao.deleteFactorDetail(productId)

    suspend fun sendFactor(request: FinalFactorRequest): Response<Any> {
        return api.sendFactor(request)
    }

 /*   suspend fun insertOrUpdateFactorDetail(details: List<FactorDetailEntity>) {
        if (details.id == null) {
            factorDao.insertFactorDetail(item)
        } else {
            factorDao.updateFactorDetail(item)
        }
    }*/

    suspend fun saveFactorDetails(details: List<FactorDetailEntity>) =
        factorDao.insertFactorDetail(details)
}