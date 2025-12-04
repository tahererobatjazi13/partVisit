package com.partsystem.partvisitapp.feature.create_order.ui

import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import javax.inject.Inject

class FactorRepository @Inject constructor(
    private val factorDao: FactorDao,
) {

    suspend fun insertFactor(factor: FactorHeaderEntity): Long {
        return factorDao.insertFactor(factor)
    }

    suspend fun getAllFactors(): List<FactorHeaderEntity> = factorDao.getAllFactors()

}
