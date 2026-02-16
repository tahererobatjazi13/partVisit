package com.partsystem.partvisitapp.feature.create_order.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.withTransaction
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import com.partsystem.partvisitapp.feature.create_order.model.ApiResponse
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorRequestDto
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderDbModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FactorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val discountRepository: DiscountRepository,
    private val factorDao: FactorDao,
    private val appDatabase: AppDatabase,
) {


    suspend fun getAllFactors(): List<FactorHeaderEntity> = factorDao.getAllFactors()

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

    fun getMaxFactorDetailId(): LiveData<Int> {
        return factorDao.getMaxFactorDetailId()
    }
    suspend fun getFactorHeaderById(factorId: Int): FactorHeaderEntity? {
        return factorDao.getFactorHeaderById(factorId)
    }

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

    /* suspend fun saveFactorDetail(detail: FactorDetailEntity): Long =
         factorDao.insertFactorDetail(detail)
 */

    // تعداد آیتم‌های سبد خرید (برای badge)
    fun getFactorItemCount(factorId: Int): LiveData<Int> {
        return factorDao.getFactorItemCount(factorId)
    }


    fun getFactorDetailByFactorIdAndProductId(
        factorId: Int,
        productId: Int
    ): Flow<FactorDetailEntity> {
        return factorDao.getFactorDetailByFactorIdAndProductId(factorId, productId)
    }

    fun getFactorDetailByFactorIdAndProductIdAsFlow(
        factorId: Int,
        productId: Int
    ): Flow<FactorDetailEntity?> {
        return factorDao.getFactorDetailByFactorIdAndProductId(factorId, productId)
    }
    suspend fun saveFactorGift(gift: FactorGiftInfoEntity): Long = factorDao.insertFactorGift(gift)
    suspend fun getFactorGifts(factorId: Int) = factorDao.getFactorGifts(factorId)

  /*  suspend fun getFactorDiscounts(factorId: Int, factorDetailId: Int?): List<FactorDiscountEntity> =
        factorDao.getFactorDiscounts(factorId, factorDetailId!!)
*/

    suspend fun getFactorDiscounts(factorId: Int, factorDetailId: Int?): List<FactorDiscountEntity> {
        return when {
            factorDetailId == null -> factorDao.getFactorLevelDiscounts(factorId)
            else -> factorDao.getDetailLevelDiscounts(factorId, factorDetailId)
        }
    }

    fun getFactorDiscountsLive(
        factorId: Int,
        factorDetailId: Int
    ): Flow<List<FactorDiscountEntity>> =
        factorDao.getFactorDiscountsLive(factorId, factorDetailId)

    suspend fun deleteFactor(factorId: Int) = factorDao.deleteFactor(factorId)

    fun getCount() = factorDao.getCount()

    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity? =
        factorDao.getHeaderByLocalId(localId)

    fun getAllFactorDetails(): Flow<List<FactorDetailEntity>> =
        factorDao.getAllFactorDetails()

    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>> =
        factorDao.getFactorDetails(factorId)

fun getAllFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>> =
        factorDao.getAllFactorDetails(factorId)


    // اضافه یا ویرایش آیتم سبد
    suspend fun upsertFactorDetail(detail: FactorDetailEntity) {
        factorDao.upsertFactorDetail(detail)
    }

    /*  suspend fun upsertFactorDetail(detail: FactorDetailEntity) {
          factorDao.upsertFactorDetail(detail)
      }
  */
    suspend fun getMaxSortCode(factorId: Int): Int {
        return factorDao.getMaxSortCode(factorId)
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

    suspend fun sendFactorToServer(
        factors: List<FinalFactorRequestDto>
    ): NetworkResult<ApiResponse> {

        return try {
            Log.d("FINAL_json2", factors.toString())

            val response = api.sendFactorToServer(factors)
            val body = response.body()

            if (response.isSuccessful && body != null && body.isSuccess) {
                NetworkResult.Success(body, body.message)
            } else {
                val errorMessage = if (body != null && !body.isSuccess) {
                    // پیام خطا از سرور
                    body.message ?: ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                } else {
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                }
                NetworkResult.Error(errorMessage)
            }

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    fun getFactorDetailUi(factorId: Int): LiveData<List<FactorDetailUiModel>> =
        factorDao.getFactorDetailUi(factorId).asLiveData()

    fun getAllHeaderUi(): Flow<List<FactorHeaderDbModel>> =
        factorDao.getFactorHeaderDbList()

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


    /*  fun getDetails(factorId: Int): LiveData<List<FactorDetailEntity>> =
          factorDao.getDetailsByFactorId(factorId)
*/
/*
    suspend fun addOrUpdateDetail(
        detail: FactorDetailEntity
        */
/* factorId: Int,
         productId: Int,
         actId: Int?,
         anbarId: Int?,
         unit1Value: Double,
         packingValue: Double,
         unit2Value: Double,
         price: Double,
         packingId: Int,
         vat: Double,
         unit1Rate: Double*//*

    ) {

        val existing =
            factorDao.getDetailByFactorAndProduct(detail.factorId, detail.productId)

        // val price = Math.round(productRate * finalUnit1).toDouble()

        if (existing != null) {
            Log.d("productdetailunit1Value", detail.unit1Value.toString())
            Log.d("productdetaildetailid", detail.id.toString())
            Log.d("productdetailpackingValuel", detail.packingValue.toString())

            //  UPDATE
            val updated = existing.copy(
                id = detail.id,
                unit1Value = detail.unit1Value,
                packingValue = detail.packingValue,
                packingId = detail.packingId,
                price = detail.price,
                vat = detail.vat,
                unit1Rate = detail.unit1Rate
            )
            factorDao.upsertFactorDetail(updated)

        } else {
            //  INSERT
            val nextSort =
                (factorDao.getMaxSortCode(detail.factorId) ) + 1
            Log.d("productdetailunit1Value00", detail.unit1Value.toString())
            Log.d("productdetaildetailid00", detail.id.toString())
            Log.d("productdetailpackingValuel00", detail.packingValue.toString())

            val detail = FactorDetailEntity(
                id = detail.id,
                factorId = detail.factorId,
                sortCode = nextSort,
                productId = detail.productId,
                actId = detail.actId,
                anbarId = detail.anbarId,
                unit1Value = detail.unit1Value,
                packingValue = detail.packingValue,
                unit2Value = detail.unit2Value,
                price = detail.price,
                packingId = detail.packingId,
                vat = detail.vat,
                unit1Rate = detail.unit1Rate
            )
            factorDao.upsertFactorDetail(detail)
        }
    }
*/

    /*suspend fun saveFactorDetailWithDiscounts(
        detail: FactorDetailEntity,
        factorHeader: FactorHeaderEntity,
        applyKind: Int = DiscountApplyKind.ProductLevel.ordinal
    ) {
        // 1. ذخیره ردیف
        addOrUpdateDetail(detail)

        // 2. محاسبه و ذخیره تخفیف‌ها
        discountRepository.calculateDiscountInsert(applyKind, factorHeader, detail)
    }*/


    suspend fun addOrUpdateDetail(detail: FactorDetailEntity): Int {
        return withContext(Dispatchers.IO) {
            val existing = factorDao.getDetailByFactorAndProduct(detail.factorId, detail.productId)
            if (existing != null) {
                val updated = existing.copy(
                    unit1Value = detail.unit1Value,
                    packingValue = detail.packingValue,
                    packingId = detail.packingId,
                    price = detail.price,
                    vat = detail.vat,
                    unit1Rate = detail.unit1Rate
                )
                factorDao.update(updated) // فقط متد ساده update فراخوانی شود
                updated.id
            } else {
                appDatabase.withTransaction {

                    val currentMax = factorDao.getMaxSortCode(detail.factorId)
                    Log.d("SortCodeDebug", "Current max sortCode for factor ${detail.factorId}: $currentMax")

                    val nextSortCode = currentMax + 1
                    Log.d("SortCodeDebug", "Inserting new detail with sortCode: $nextSortCode")

                    factorDao.insert(detail.copy(id = 0, sortCode = nextSortCode)).toInt()


             /*   val nextSortCode = factorDao.getMaxSortCode(detail.factorId) + 1
                factorDao.insert(detail.copy(id = 0, sortCode = nextSortCode)).toInt()*/
            }
            }
        }
    }

    // در FactorRepository.kt
    suspend fun updateVatForDetail(detailId: Int, vat: Double) {
        withContext(Dispatchers.IO) {
            factorDao.updateVat(detailId, vat)
        }
    }

    /*
        suspend fun addOrUpdateDetail(detail: FactorDetailEntity): Int {
            return withContext(Dispatchers.IO) {
                val existing = factorDao.getDetailByFactorAndProduct(detail.factorId, detail.productId)

                if (existing != null) {
                    // Update
                    val updated = existing.copy(
                        unit1Value = detail.unit1Value,
                        packingValue = detail.packingValue,
                        packingId = detail.packingId,
                        price = detail.price,
                        vat = detail.vat,
                        unit1Rate = detail.unit1Rate
                    )
                    factorDao.upsertFactorDetail(updated)
                    updated.id // 👈 id موجود
                } else {
                    // Insert
                    val nextSortCode = factorDao.getMaxSortCode(detail.factorId) + 1
                    val newDetail = detail.copy(
                        id = 0, // Room خودش id تولید می‌کند
                        sortCode = nextSortCode
                    )
                    val insertedId = factorDao.upsertFactorDetail(newDetail).toInt() // برگرداندن id جدید
                    insertedId
                }
            }
        }
    */


    // در FactorRepository
    suspend fun getTotalDiscountForDetail(detailId: Int): Double = withContext(Dispatchers.IO) {
        factorDao.getTotalDiscountForDetail(detailId) ?: 0.0
    }

    suspend fun getTotalAdditionForDetail(detailId: Int): Double = withContext(Dispatchers.IO) {
        factorDao.getTotalAdditionForDetail(detailId) ?: 0.0
    }

    suspend fun updateFactorDetail(detail: FactorDetailEntity) = withContext(Dispatchers.IO) {
        factorDao.updateFactorDetail(detail)
    }


    suspend fun getTotalFactorLevelDiscount(factorId: Int): Double? {
        return factorDao.getTotalFactorLevelDiscount(factorId)
    }

    suspend fun getTotalProductLevelDiscount(factorId: Int): Double? {
        return factorDao.getTotalProductLevelDiscount(factorId)
    }

    suspend fun deleteFactorLevelDiscounts(factorId: Int) {
        factorDao.deleteFactorLevelDiscounts(factorId)
    }
}