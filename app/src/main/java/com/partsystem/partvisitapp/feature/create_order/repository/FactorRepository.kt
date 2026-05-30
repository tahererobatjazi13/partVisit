package com.partsystem.partvisitapp.feature.create_order.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.database.dao.ApplicationSettingDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.VisitorDao
import com.partsystem.partvisitapp.feature.create_order.model.CustomerVisitorStatus
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.CustomerCreditKind
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import com.partsystem.partvisitapp.core.utils.FactorFormKind
import com.partsystem.partvisitapp.core.utils.MessageKind
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.feature.create_order.model.ApiResponse
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorRequestDto
import com.partsystem.partvisitapp.feature.create_order.model.ValidateCredit
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderDbModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import javax.inject.Inject

class FactorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val factorDao: FactorDao,
    private val applicationSettingDao: ApplicationSettingDao,
    private val customerDao: CustomerDao,
    private val visitorDao: VisitorDao,
    private val appDatabase: AppDatabase,
) {

    suspend fun deleteFactorDetail(productId: Int) =
        factorDao.deleteFactorDetail(productId)

    fun getMaxFactorDetailId(): LiveData<Int> {
        return factorDao.getMaxFactorDetailId()
    }

    suspend fun getFactorHeaderById(factorId: Int): FactorHeaderEntity? {
        return factorDao.getFactorHeaderById(factorId)
    }

    suspend fun saveFactorHeader(header: FactorHeaderEntity): Long {
        return factorDao.insertFactorHeader(header)
    }

    suspend fun updateFactorHeader(header: FactorHeaderEntity) {
        factorDao.updateFactorHeader(header)
    }

    suspend fun updateHasDetail(factorId: Int, hasDetail: Boolean) {
        factorDao.updateHasDetail(factorId, hasDetail)
    }

    suspend fun getDetailCountForFactor(factorId: Int): Int {
        return factorDao.getDetailCountForFactor(factorId)
    }

    suspend fun updateHeader(header: FactorHeaderEntity) = factorDao.updateHeader(header)

    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity> = factorDao.getHeaderById(id)

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

    suspend fun getFactorGifts(factorId: Int) = factorDao.getFactorGifts(factorId)

    suspend fun getFactorDiscounts(
        factorId: Int,
        factorDetailId: Int?
    ): List<FactorDiscountEntity> {
        return when {
            factorDetailId == null -> factorDao.getFactorLevelDiscounts(factorId)
            else -> factorDao.getDetailLevelDiscounts(factorId, factorDetailId)
        }
    }

    suspend fun deleteFactor(factorId: Int) = factorDao.deleteFactor(factorId)

    fun getCount() = factorDao.getCount()

    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity? =
        factorDao.getHeaderByLocalId(localId)

    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>> =
        factorDao.getFactorDetails(factorId)

    fun getAllFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>> =
        factorDao.getAllFactorDetails(factorId)

    // اضافه یا ویرایش آیتم سبد
    suspend fun upsertFactorDetail(detail: FactorDetailEntity) {
        factorDao.upsertFactorDetail(detail)
    }

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

    suspend fun getFactorDetailsRaw(factorId: Int): List<FactorDetailEntity> =
        factorDao.getFactorDetailsRaw(factorId)

    fun getFactorDetailUiWithAggregatedDiscounts(factorId: Int): Flow<List<FactorDetailUiModel>> {
        return factorDao.getFactorDetailUi(factorId).map { details ->
            details.groupBy { it.id }
                .map { (_, items) ->
                    val firstItem = items.first()
                    firstItem.copy(
                        discountPrice = items.sumOf { it.discountPrice } // جمع تخفیف‌ها
                    )
                }
                .sortedBy { it.sortCode }
        }
    }

    fun getAllHeaderUi(visitorId: Int): Flow<List<FactorHeaderDbModel>> =
        factorDao.getFactorHeaderDbList(visitorId)

    suspend fun getHeaderByIdSuspend(factorId: Int) =
        factorDao.getHeaderByIdSuspend(factorId)

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
                factorDao.update(updated)
                updated.id
            } else {
                appDatabase.withTransaction {

                    val currentMax = factorDao.getMaxSortCode(detail.factorId)

                    val nextSortCode = currentMax + 1

                    factorDao.insert(detail.copy(id = 0, sortCode = nextSortCode)).toInt()

                }
            }
        }
    }

    suspend fun updateVatForDetail(detailId: Int, vat: Double) {
        withContext(Dispatchers.IO) {
            factorDao.updateVat(detailId, vat)
        }
    }

    suspend fun getTotalDiscountForDetail(detailId: Int): Double = withContext(Dispatchers.IO) {
        factorDao.getTotalDiscountForDetail(detailId) ?: 0.0
    }

    suspend fun getTotalAdditionForDetail(detailId: Int): Double = withContext(Dispatchers.IO) {
        factorDao.getTotalAdditionForDetail(detailId) ?: 0.0
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

    suspend fun getSumPriceAfterVatByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterVat() }

    suspend fun getSumPriceAfterDiscountByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterDiscount() }

    suspend fun getSumUnit1ValueByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.unit1Value }

    suspend fun getHasTaxConnection(): Boolean {
        val setting = applicationSettingDao.getSettingByName("HasTaxConnection")
        return setting?.value.equals("true", ignoreCase = true)
    }

    suspend fun getControlCustomerLocation(): Boolean {
        val setting = applicationSettingDao.getSettingByName("ControlCustomerLocation")
        return setting?.value.equals("true", ignoreCase = true)
    }

    // متدی برای دریافت وضعیت خطا و هشدار مشتری
    fun getCustomerErrorStatus(customerId: Int): Flow<CustomerVisitorStatus?> {
        return customerDao.getCustomerErrorStatus(customerId)
    }


    suspend fun fetchCustomerCredit(
        customerId: Int,
        persianDate: String,
        kind: CustomerCreditKind
    ): NetworkResult<List<ValidateCredit>> {
        return withContext(Dispatchers.IO) {
            try {

                val response = api.validateCreditCustomer(customerId, persianDate, kind.value)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    NetworkResult.Success(body)
                } else {
                    val errorMsg = ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                    NetworkResult.Error(errorMsg)
                }

            } catch (ex: Exception) {
                val errorMsg = getExceptionMessage(context, ex)
                NetworkResult.Error(errorMsg)
            }
        }
    }

    suspend fun validateCreditResults(
        allCustomerCredit: List<ValidateCredit>,
        kind: CustomerCreditKind,
        formKind: FactorFormKind, customerId: Int
    ): Pair<Boolean, String> {

        var isValidResult = true
        var messageResult = ""
        var messageKind = MessageKind.Warning

        if (allCustomerCredit.isEmpty()) {
            return Pair(true, "")
        }
        var finalPriceWithNewFactor = 0.0
        var monthFactorCount = 0
        var yearFactorCount = 0
        var monthTotalFinalPrice = 0.0
        var yearTotalFinalPrice = 0.0

        if (allCustomerCredit.any { it.hasErrorOrder || it.hasWarningOrder }) {
            allCustomerCredit.filter { it.mandeh != null && it.mandeh > 0 }.forEach { row ->
                var isRowValid = true
                when (row.kind) {
                    CustomerCreditKind.Customer.value -> {
                        val year = getTodayPersianDateLatin().substring(0, 4)
                        val month = getTodayPersianDateLatin().substring(5, 7)

                        val customerFactorStats = factorDao
                            .getCustomerFactorStats(customerId, year, month)
                            .firstOrNull()


                        val totalFinalPrice = customerFactorStats?.totalFinalPrice ?: 0.0
                        monthFactorCount = customerFactorStats?.monthFactorCount ?: 0
                        yearFactorCount = customerFactorStats?.yearFactorCount ?: 0
                        monthTotalFinalPrice = customerFactorStats?.monthTotalFinalPrice ?: 0.0
                        yearTotalFinalPrice = customerFactorStats?.yearTotalFinalPrice ?: 0.0

                        finalPriceWithNewFactor = totalFinalPrice + row.mandeh

                        Log.d(
                            "finalPriceTotalFinalPrice",
                            totalFinalPrice.toString()
                        )
                        Log.d(
                            "finalPriceRowMandeh", row.mandeh.toString()
                        )
                        Log.d("finalPriceWithNewFactor", finalPriceWithNewFactor.toString())


                        when (row.kind) {
                            // These values (0, 1, 2, 3) should correspond to CustomerCreditSarfaslKind enum values in C#
                            0 -> if (finalPriceWithNewFactor > row.accountRemain) isRowValid =
                                false // Account
                            1 -> if (finalPriceWithNewFactor > row.asnadRemain) isRowValid =
                                false   // AsnadDaryaftani
                            2 -> if (finalPriceWithNewFactor > row.totalRemain) isRowValid =
                                false  // TotalAccount
                            3 -> if (finalPriceWithNewFactor > row.maxValidAsnad) isRowValid =
                                false // AsnadVakhasti
                            else -> {
                            }
                        }
                    }
                }

                if (!isRowValid) {

                    messageResult += ",${row.kindDescription} (${
                        DecimalFormat("#,###,###,###").format(
                            finalPriceWithNewFactor
                        )
                    })"
                }
            }

            if (messageResult.isNotBlank()) {
                isValidResult = false
                val entityType =
                    if (kind == CustomerCreditKind.Customer) " مشتری " else " بازاریاب "
                messageResult =
                    "مانده ${entityType.trim()} ${messageResult.substring(1)} تمام شده است ."

                messageKind =
                    if (formKind == FactorFormKind.Factor || formKind == FactorFormKind.FactorDistribute) {
                        if (allCustomerCredit.any { it.hasErrorFactor }) MessageKind.Error else MessageKind.Warning
                    } else {
                        if (allCustomerCredit.any { it.hasErrorOrder }) MessageKind.Error else MessageKind.Warning
                    }
            }
        }

        if (isValidResult) {
            val item = allCustomerCredit.firstOrNull()
            if (item != null) {
                if (item.hasErrorOrder || item.hasWarningOrder) {

                    val finalMonthFactorCount = monthFactorCount + item.factorCount
                    val finalYearFactorCount = yearFactorCount + item.factorCountYear
                    val finalMonthTotalFinalPrice = monthTotalFinalPrice + item.maxSale
                    val finalYearTotalFinalPrice = yearTotalFinalPrice + item.maxSaleYear

                    Log.d(
                        "finalFactorCountMonth",
                        finalMonthFactorCount.toString()
                    )
                    Log.d(
                        "finalFactorCountYear",
                        finalYearFactorCount.toString()
                    )

                    Log.d(
                        "finalFactorPrice Month",
                        finalMonthTotalFinalPrice.toString()
                    )
                    Log.d(
                        "finalFactorPrice Year",
                        finalYearTotalFinalPrice.toString()
                    )
                    // Monthly Factor Count
                    if (item.maxFactor < finalMonthFactorCount) {
                        isValidResult = false
                        val entityType =
                            if (kind == CustomerCreditKind.Customer) " مشتری " else " بازاریاب "
                        messageResult =
                            "تعداد فاکتورهای ${entityType.trim()} ماه از حد مجاز بیشتر می شود امکان ثبت وجود ندارد."
                        messageKind =
                            if (formKind == FactorFormKind.Factor || formKind == FactorFormKind.FactorDistribute) {
                                if (item.hasErrorFactor) MessageKind.Error else MessageKind.Warning
                            } else {
                                if (item.hasErrorOrder) MessageKind.Error else MessageKind.Warning
                            }
                    }
                    // Yearly Factor Count
                    else if (item.maxFactorYear < finalYearFactorCount) {
                        isValidResult = false
                        val entityType =
                            if (kind == CustomerCreditKind.Customer) " مشتری " else " بازاریاب "
                        messageResult =
                            "تعداد فاکتورهای ${entityType.trim()} سال از حد مجاز بیشتر می شود امکان ثبت وجود ندارد."
                        messageKind =
                            if (formKind == FactorFormKind.Factor || formKind == FactorFormKind.FactorDistribute) {
                                if (item.hasErrorFactor) MessageKind.Error else MessageKind.Warning
                            } else {
                                if (item.hasErrorOrder) MessageKind.Error else MessageKind.Warning
                            }
                    }
                    // Monthly Sales Amount
                    else if (item.maxSalePrice < finalMonthTotalFinalPrice) {
                        isValidResult = false
                        val entityType =
                            if (kind == CustomerCreditKind.Customer) " مشتری " else " بازاریاب "
                        messageResult =
                            "مبلغ فروش ماه ${entityType.trim()} از حد مجاز بیشتر می شود امکان ثبت وجود ندارد."
                        messageKind =
                            if (formKind == FactorFormKind.Factor || formKind == FactorFormKind.FactorDistribute) {
                                if (item.hasErrorFactor) MessageKind.Error else MessageKind.Warning
                            } else {
                                if (item.hasErrorOrder) MessageKind.Error else MessageKind.Warning
                            }
                    }
                    // Yearly Sales Amount
                    else if (item.maxSalePriceYear < finalYearTotalFinalPrice) {
                        isValidResult = false
                        val entityType =
                            if (kind == CustomerCreditKind.Customer) " مشتری " else " بازاریاب "
                        messageResult =
                            "مبلغ فروش سال ${entityType.trim()} از حد مجاز بیشتر می شود امکان ثبت وجود ندارد."
                        messageKind =
                            if (formKind == FactorFormKind.Factor || formKind == FactorFormKind.FactorDistribute) {
                                if (item.hasErrorFactor) MessageKind.Error else MessageKind.Warning
                            } else {
                                if (item.hasErrorOrder) MessageKind.Error else MessageKind.Warning
                            }
                    }
                }
            }
        }
        return Pair(isValidResult, messageResult)
    }



    suspend fun getAllRegisteredFactors(): List<FactorHeaderEntity> {
        return factorDao.getAllPendingSabtFactors()
    }

}
