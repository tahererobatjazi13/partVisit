package com.partsystem.partvisitapp.feature.create_order.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.AssignDirectionCustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDirectionDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.InvoiceCategoryDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.SaleCenterDao
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class HeaderOrderRepository @Inject constructor(
    private val factorDao: FactorDao,
    private val customerDao: CustomerDao,
    private val customerDirectionDao: CustomerDirectionDao,
    private val invoiceCategoryDao: InvoiceCategoryDao,
    private val patternDao: PatternDao,
    private val saleCenterDao: SaleCenterDao,
    private val assignDirectionCustomerDao: AssignDirectionCustomerDao,
    private val actDao: ActDao,
) {
   // fun insert(factor: FactorHeaderEntity) = factorDao.insertFactor(factor)

    fun update(factor: FactorHeaderEntity) = factorDao.updateFactor(factor)

    fun delete(factor: FactorHeaderEntity) = factorDao.deleteFactor(factor)

    fun getById(id: Int) = factorDao.getFactorById(id)

    fun getCustomerDirectionsByCustomer(customerId: Int): Flow<List<CustomerDirectionEntity>> =
        customerDirectionDao.getCustomerDirectionsByCustomer(customerId)


    fun getSaleCenters(invoiceCategoryId: Int): List<SaleCenterEntity> =
        saleCenterDao.getSaleCenters(invoiceCategoryId)

    suspend fun getSaleCenter(id: Int): SaleCenterEntity? {
        return saleCenterDao.getSaleCenter(id)
    }

    fun getInvoiceCategory(userId: Int): Flow<List<InvoiceCategoryEntity>> {
        return invoiceCategoryDao.getInvoiceCategory(userId)
    }

    fun getPattern(): Flow<List<PatternEntity>> =
        patternDao.getAllPatterns()

    fun getAct(): Flow<List<ActEntity>> =
        actDao.getActs()

    fun getPatternById(id: Int): LiveData<PatternEntity> = patternDao.getPatternById(id)
/*
     fun getPatternById(id: Int): PatternEntity? {
        return patternDao.getPatternById(id)
    }*/
//    suspend fun getPatterns(): List<PatternEntity> {
//        return patternDao.getAllPatterns()
//    }

    /*    suspend fun getActsByPattern(patternId: Int, actKind: Int): List<ActEntity> {
            return actDao.getActsByPattern(patternId, actKind)
        }

        suspend fun getActById(actId: Int): ActEntity? {
            return actDao.getActById(actId)
        }*/
    suspend fun getActsByPatternId(patternId: Int, kind: Int): List<ActEntity> {
        return actDao.getActsByPatternId(patternId, kind)
    }

    private var productActIdCache: Int? = null

    suspend fun getProductActId(patternId: Int): Int? {
        if (productActIdCache == null) {
            productActIdCache = actDao.getPatternDetailActId(
                patternId,
                ActKind.Product.ordinal
            )
        }
        return productActIdCache
    }

    suspend fun getActById(actId: Int): ActEntity? {
        return actDao.getActById(actId)
    }

    suspend fun clearAll() = customerDirectionDao.clearCustomerDirection()

    suspend fun getCustomerById(customerId: Int): CustomerEntity? {
        return customerDao.getCustomerById(customerId)
    }

    suspend fun getPatternsForCustomer(
        customerId: Int,
        centerId: Int?,
        invoiceCategoryId: Int?,
        settlementKind: Int,
        date: String
    ): List<PatternEntity> {

        //  Customer را از دیتابیس بخوان
        val customer = getCustomerById(customerId) ?: return emptyList()

        //  محاسبه set1
        val set1 = patternDao.filterCustomerPatterns(
            customerId = customer.id,
            customerKindId = customer.customerKindId,
            customerDegreeId = customer.degreeId ?: 0,
            customerPishehId = customer.processKindId ?: 0,
            settlementKind = settlementKind
        ).toSet()

        val set2 = patternDao.filterCenterPatterns(centerId, settlementKind).toSet()
        val set3 =
            patternDao.filterInvoiceCategoryPatterns(invoiceCategoryId, settlementKind).toSet()

        val resultIds = set1.intersect(set2).intersect(set3).toList()

        return patternDao.getPatternsFinal(resultIds, date)
    }

    suspend fun getAssignDirectionCustomerByCustomerId(customerId: Int) =
        assignDirectionCustomerDao.getAssignDirectionCustomerByCustomerId(customerId)

    suspend fun getActiveSaleCenterAnbar(saleCenterId: Int): Int {
        return saleCenterDao.getActiveSaleCenterAnbar(saleCenterId) ?: 0
    }
}
