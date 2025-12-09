package com.partsystem.partvisitapp.feature.create_order.ui

import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import javax.inject.Inject

class FactorRepository @Inject constructor(
    private val factorDao: FactorDao,
) {
    suspend fun insertFactor(factor: FactorHeaderEntity): Long {
        return factorDao.insertFactor(factor)
    }

    suspend fun getAllFactors(): List<FactorHeaderEntity> = factorDao.getAllFactors()

    fun getAllFactorDetail() = factorDao.getAllFactorDetail()

    suspend fun insertFactorDetail(item: FactorDetailEntity) =
        factorDao.insertFactorDetail(item)

    suspend fun deleteFactorDetail(productId: Int) =
        factorDao.deleteFactorDetail(productId)
}



