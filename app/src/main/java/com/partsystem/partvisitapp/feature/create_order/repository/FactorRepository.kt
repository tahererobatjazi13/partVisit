package com.partsystem.partvisitapp.feature.create_order.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorRequestDto
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FactorRepository @Inject constructor(
    private val api: ApiService,
    private val factorDao: FactorDao,
) {


    suspend fun getAllFactors(): List<FactorHeaderEntity> = factorDao.getAllFactors()

    fun getAllFactorDetail() = factorDao.getAllFactorDetail()


    suspend fun deleteFactorDetail(productId: Int) =
        factorDao.deleteFactorDetail(productId)

    /*   suspend fun insertOrUpdateFactorDetail(details: List<FactorDetailEntity>) {
           if (details.id == null) {
               factorDao.insertFactorDetail(item)
           } else {
               factorDao.updateFactorDetail(item)
           }
       }*/

    suspend fun insertOrUpdateFactorDetail(detail: FactorDetailEntity) {
        factorDao.insertOrUpdate(detail)
    }

    suspend fun saveFactorDetails(details: List<FactorDetailEntity>) =
        factorDao.insertFactorDetail(details)

    //////////////


    // DB ops
    suspend fun saveFactorHeader(header: FactorHeaderEntity): Long {
        return factorDao.insertFactorHeader(header) // بدون withContext
    }

    suspend fun updateFactorHeader(header: FactorHeaderEntity) {
        factorDao.updateFactorHeader(header)
    }

    fun getAllHeaders(): Flow<List<FactorHeaderEntity>> = factorDao.getAllHeaders()

    suspend fun updateHeader(header: FactorHeaderEntity) = factorDao.updateHeader(header)

    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity> = factorDao.getHeaderById(id)

    suspend fun saveFactorDetail(detail: FactorDetailEntity): Long =
        factorDao.insertFactorDetail(detail)


    // تعداد آیتم‌های سبد خرید (برای badge)
    fun getFactorItemCount(factorId: Int): LiveData<Int> {
        return factorDao.getFactorItemCount(factorId)
    }

    suspend fun saveFactorGift(gift: FactorGiftInfoEntity): Long = factorDao.insertFactorGift(gift)
    suspend fun getFactorGifts(factorId: Int) = factorDao.getFactorGifts(factorId)

    suspend fun deleteHeader(factorId: Int) = factorDao.deleteHeader(factorId)

    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity? =
        factorDao.getHeaderByLocalId(localId)


    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>> =
        factorDao.getFactorDetails(factorId)


    // اضافه یا ویرایش آیتم سبد
    suspend fun upsertFactorDetail(detail: FactorDetailEntity) {
        factorDao.upsert(detail)
    }

    // حذف آیتم وقتی مقدار صفر شد
    suspend fun deleteFactorDetail(
        factorId: Int,
        productId: Int
    ) {
        factorDao.deleteByFactorAndProduct(factorId, productId)
    }

    // پاک کردن کل سبد (اختیاری)
    suspend fun clearFactor(factorId: Int) {
        factorDao.clearFactor(factorId)
    }
    /*
        // Network
        suspend fun sendFactor(request: FinalFactorRequest) = api.sendFactor(request)
    */

    suspend fun sendFactorToServer(request: FinalFactorRequestDto) =
        api.sendFactor(listOf(request))

    fun getFactorDetailUi(factorId: Int): LiveData<List<FactorDetailUiModel>> =
        factorDao.getFactorDetailUi(factorId).asLiveData()

    fun getAllHeaderUi(): Flow<List<FactorHeaderUiModel>> =
        factorDao.getFactorHeaderUiList()


    suspend fun getSumPriceByProductIds(factorId: Int, productIds: List<Int>): Double {
        // اگر لیست خالی بود، نتیجه 0 است (مثل کد جاوا)
        if (productIds.isEmpty()) return 0.0

        return factorDao.getSumPriceByProductIds(factorId, productIds) ?: 0.0
    }

    private suspend fun getSumByField(
        factorId: Int,
        productIds: List<Int>,
        fieldSelector: (FactorDetailEntity) -> Double
    ): Double {

        // اگر لیست محصولات خالی باشد، نتیجه 0 است
        if (productIds.isEmpty()) return 0.0
        val details = factorDao.getNonGiftFactorDetailsByProductIds(factorId, productIds)
        return details.sumOf(fieldSelector)
    }

    // سپس:
    suspend fun getSumPriceAfterVatByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterVat() }

    suspend fun getSumPriceAfterDiscountByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterDiscount() }

    suspend fun getSumUnit1ValueByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.unit1Value }


    /**
     * معادل q.getSumUnitValueFactor(factorId) در جاوا
     */
    suspend fun getSumUnit1ValueByFactorId(factorId: Int): Double {
        return factorDao.getSumUnit1ValueByFactorId(factorId)
    }
}