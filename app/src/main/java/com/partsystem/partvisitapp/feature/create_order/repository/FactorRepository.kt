package com.partsystem.partvisitapp.feature.create_order.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.gson.Gson
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
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderDbModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class FactorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val factorDao: FactorDao,
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

    suspend fun saveFactorGift(gift: FactorGiftInfoEntity): Long = factorDao.insertFactorGift(gift)
    suspend fun getFactorGifts(factorId: Int) = factorDao.getFactorGifts(factorId)

    suspend fun getFactorDiscounts(factorId: Int, factorDetailId: Int): List<FactorDiscountEntity> =
        factorDao.getFactorDiscounts(factorId, factorDetailId)

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


    suspend fun sendFactorToServer(
        factors: List<FinalFactorRequestDto>
    ): NetworkResult<ApiResponse> {

        return try {
            Log.d("FINAL_json2", factors.toString())

            val response = api.sendFactorToServer(factors)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                NetworkResult.Error(errorMessage)
            }

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }


    /*
        suspend fun sendFactorToServer(json: String): Response<Any> {
        //    val request = Gson().fromJson(json, Array<FinalFactorRequestDto>::class.java).toList()
            Log.d("FINAL_request22", json)
            return api.sendFactor(json)
        }*/


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

}