package com.partsystem.partvisitapp.feature.create_order.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.FinalFactorRequest
import com.partsystem.partvisitapp.core.network.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
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

    suspend fun saveFactorDetails(details: List<FactorDetailEntity>) =
        factorDao.insertFactorDetail(details)

    //////////////


    // DB ops

     fun saveFactorHeader(header: FactorHeaderEntity): Long =
        factorDao.insertFactorHeader(header)


    fun getAllHeaders(): LiveData<List<FactorHeaderEntity>> =
        factorDao.getAllHeaders()

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

    suspend fun deleteHeader(headerId: Long) = factorDao.deleteHeader(headerId)
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
    // Network
    suspend fun sendFactor(request: FinalFactorRequest) = api.sendFactor(request)




}