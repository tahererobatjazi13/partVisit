package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDirectionDao
import com.partsystem.partvisitapp.core.database.dao.InvoiceCategoryDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class HeaderOrderRepository @Inject constructor(
    private val customerDirectionDao: CustomerDirectionDao,
    private val invoiceCategoryDao: InvoiceCategoryDao,
    private val patternDao: PatternDao,
    private val actDao: ActDao,
) {

    fun getDirectionsByCustomer(customerId: Int): Flow<List<CustomerDirectionEntity>> =
        customerDirectionDao.getDirectionsByCustomer(customerId)

    fun getInvoiceCategory(): Flow<List<InvoiceCategoryEntity>> =
        invoiceCategoryDao.getAllInvoiceCategory()

    fun getPattern(): Flow<List<PatternEntity>> =
        patternDao.getAllPatterns()

    fun getAct(): Flow<List<ActEntity>> =
        actDao.getAllActs()

    suspend fun clearAll() = customerDirectionDao.clearCustomerDirection()
}
