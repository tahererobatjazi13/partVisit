package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.AssignDirectionCustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDirectionDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.InvoiceCategoryDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class HeaderOrderRepository @Inject constructor(
    private val factorDao: FactorDao,
    private val customerDirectionDao: CustomerDirectionDao,
    private val invoiceCategoryDao: InvoiceCategoryDao,
    private val patternDao: PatternDao,
    private val assignDirectionCustomerDao: AssignDirectionCustomerDao,
    private val actDao: ActDao,
) {
    fun insert(factor: FactorEntity) = factorDao.insertFactor(factor)

    fun update(factor: FactorEntity) = factorDao.updateFactor(factor)

    fun delete(factor: FactorEntity) = factorDao.deleteFactor(factor)

    fun getById(id: Int) = factorDao.getFactorById(id)

    fun getCustomerDirectionsByCustomer(customerId: Int): Flow<List<CustomerDirectionEntity>> =
        customerDirectionDao.getCustomerDirectionsByCustomer(customerId)

    fun getInvoiceCategory(): Flow<List<InvoiceCategoryEntity>> =
        invoiceCategoryDao.getAllInvoiceCategory()

    fun getPattern(): Flow<List<PatternEntity>> =
        patternDao.getAllPatterns()

    fun getAct(): Flow<List<ActEntity>> =
        actDao.getAllActs()

    suspend fun clearAll() = customerDirectionDao.clearCustomerDirection()

    /////
    fun getPatternsForCustomer(
        customer: CustomerEntity,
        centerId: Int?,
        invoiceCategoryId: Int?,
        // processId: Int?,
        settlementKind: Int,
        date: String
    ): List<PatternEntity> {

        val set1 = patternDao.filterCustomerPatterns(
            customerId = customer.id,
            customerKindId = customer.customerKindId,
            customerDegreeId = customer.degreeId,
            customerPishehId = customer.processKindId,
            settlementKind = settlementKind
        ).toSet()

        val set2 = patternDao.filterCenterPatterns(centerId, settlementKind).toSet()
        val set3 =
            patternDao.filterInvoiceCategoryPatterns(invoiceCategoryId, settlementKind).toSet()
        // val set4 = patternDao.filterProcessPatterns(processId, settlementKind).toSet()

        val resultIds = set1.intersect(set2).intersect(set3)/*.intersect(set4)*/.toList()

        return patternDao.getPatternsFinal(resultIds, date)
    }

    suspend  fun getAssignDirectionCustomerByCustomerId(customerId: Int) =
        assignDirectionCustomerDao.getAssignDirectionCustomerByCustomerId(customerId)


}
