package com.partsystem.partvisitapp.feature.create_order.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.Event
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorDetailDto
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorDiscountDto
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorGiftDto
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorRequestDto
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.repository.DiscountRepository
import com.partsystem.partvisitapp.feature.create_order.repository.FactorRepository
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class FactorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,

    private val factorRepository: FactorRepository,
    private val discountRepository: DiscountRepository,
    private val appDatabase: AppDatabase,
    val productRepository: ProductRepository,
) : ViewModel() {
    private val _factorHeader = MutableStateFlow<FactorHeaderEntity?>(null)

    val factorHeader = MutableLiveData(FactorHeaderEntity())
    val factorDetails = MutableLiveData<MutableList<FactorDetailEntity>>(mutableListOf())
    val factorGifts = MutableLiveData<MutableList<FactorGiftInfoEntity>>(mutableListOf())


    private val factorItems = mutableMapOf<Int, FactorDetailEntity>()
    var enteredProductPage = false

    fun resetHeader() {
        factorHeader.value = FactorHeaderEntity()
    }

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    fun deleteFactorDetail(item: FactorDetailUiModel) {
        viewModelScope.launch {
            factorRepository.deleteFactorDetail(item.productId!!)
            val detailCount =
                factorRepository.getDetailCountForFactor(item.factorId)

            val hasDetail = detailCount > 0

            factorRepository.updateHasDetail(
                item.factorId,
                hasDetail
            )
        }
    }

    fun deleteFactor(factorId: Int) = viewModelScope.launch {
        factorRepository.deleteFactor(factorId)
    }

    suspend fun getFactorHeaderById(factorId: Int): FactorHeaderEntity? {
        return factorRepository.getFactorHeaderById(factorId)
    }

    fun updateHeader(
        customerId: Int? = null,
        directionDetailId: Int? = null,
        invoiceCategoryId: Int? = null,
        saleCenterId: Int? = null,
        defaultAnbarId: Int? = null,
        patternId: Int? = null,
        actId: Int? = null,
        settlementKind: Int? = null,
        persianDate: String? = null,
        description: String? = null,
        createDate: String? = null,
        dueDate: String? = null,
        deliveryDate: String? = null,
        hasDetail: Boolean? = null,
        finalPrice: Double? = null,
        productSelectionType: String? = null,
        sabt: Int? = null
    ) {
        val current = factorHeader.value ?: FactorHeaderEntity()
        factorHeader.value = current.copy(
            customerId = customerId ?: current.customerId,
            directionDetailId = directionDetailId ?: current.directionDetailId,
            invoiceCategoryId = invoiceCategoryId ?: current.invoiceCategoryId,
            saleCenterId = saleCenterId ?: current.saleCenterId,
            defaultAnbarId = defaultAnbarId ?: current.defaultAnbarId,
            patternId = patternId ?: current.patternId,
            actId = actId ?: current.actId,
            settlementKind = settlementKind ?: current.settlementKind,
            persianDate = persianDate ?: current.persianDate,
            description = description ?: current.description,
            createDate = createDate ?: current.createDate,
            dueDate = dueDate ?: current.dueDate,
            deliveryDate = deliveryDate ?: current.deliveryDate,
            hasDetail = hasDetail ?: current.hasDetail,
            finalPrice = finalPrice ?: current.finalPrice,
            productSelectionType = productSelectionType ?: current.productSelectionType,
            sabt = sabt ?: current.sabt
        )
        _factorHeader.value = factorHeader.value
    }

    fun loadProduct(productId: Int, actId: Int): ProductWithPacking? {
        return productRepository.getProductByActId(productId, actId)
    }

    fun getProductByActId(productId: Int, actId: Int): LiveData<ProductWithPacking> {
        return productRepository.getProductByActId2(productId, actId).asLiveData()
    }

    suspend fun getProductRate(productId: Int, actId: Int): Double? {
        return productRepository.getProductByActId2(productId, actId)
            .map { it.rate }
            .firstOrNull()
    }

    suspend fun updateFactorHeader(header: FactorHeaderEntity) {
        factorRepository.updateFactorHeader(header)
    }

    private val _currentHeader = MutableLiveData<FactorHeaderEntity?>()
    val currentHeader: LiveData<FactorHeaderEntity?> = _currentHeader

    private val _header = MutableLiveData<FactorHeaderEntity?>()
    val header: LiveData<FactorHeaderEntity?> = _header

    private val _details = MutableLiveData<List<FactorDetailEntity>>(emptyList())
    val details: LiveData<List<FactorDetailEntity>> = _details

    private val _discounts = MutableLiveData<List<FactorDiscountEntity>>(emptyList())
    val discounts: LiveData<List<FactorDiscountEntity>> = _discounts

    private val _gifts = MutableLiveData<List<FactorGiftInfoEntity>>(emptyList())
    val gifts: LiveData<List<FactorGiftInfoEntity>> = _gifts

    val currentFactorId = MutableLiveData<Long>(0L)
    val headerId = MutableLiveData<Int?>()

    // فلگ برای ردیابی حذف دستی تخفیف توسط کاربر
    private val _discountManuallyRemoved = MutableStateFlow(false)
    val discountManuallyRemoved = _discountManuallyRemoved.asStateFlow()
    fun markDiscountApplied() {
        _discountManuallyRemoved.value = false
    }

    fun markDiscountRemoved() {
        _discountManuallyRemoved.value = true
    }

    suspend fun saveHeaderAndGetId(header: FactorHeaderEntity): Long {
        return factorRepository.saveFactorHeader(header)
    }

    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity> =
        factorRepository.getHeaderById(id)

    private val _allHeaders = MutableLiveData<List<FactorHeaderUiModel>>()
    private val _filteredHeaders = MutableLiveData<List<FactorHeaderUiModel>>()
    val filteredHeaders: LiveData<List<FactorHeaderUiModel>> = _filteredHeaders

    private var lastQuery: String = ""

    init {
        viewModelScope.launch {
            factorRepository.getAllHeaderUi().collectLatest { dbList ->

                val uiList = dbList.map {
                    FactorHeaderUiModel(
                        factorId = it.factorId,
                        customerName = it.customerName,
                        patternName = it.patternName,
                        persianDate = it.persianDate,
                        createTime = it.createTime,
                        finalPrice = it.finalPrice,
                        hasDetail = it.hasDetail,
                        actId = it.actId,
                        sabt = it.sabt,
                        isSending = false
                    )
                }
                _allHeaders.value = uiList
                applyFilter(lastQuery)
            }
        }
    }

    fun filterHeaders(query: String) {
        lastQuery = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val list = _allHeaders.value ?: emptyList()

        _filteredHeaders.value =
            if (query.isBlank()) list
            else list.filter {
                it.customerName?.contains(query, true) == true ||
                        it.patternName?.contains(query, true) == true ||
                        it.factorId.toString().contains(query)
            }
    }

    fun addOrUpdateFactorDetail(detail: FactorDetailEntity) {
        viewModelScope.launch {
            if (detail.unit1Value!! <= 0 && detail.packingValue!! <= 0) {
                factorRepository.deleteFactorDetail(detail.factorId, detail.productId!!)
            } else {
                factorRepository.upsertFactorDetail(detail)
            }
        }
    }

    fun getFactorDetailUi(factorId: Int): LiveData<List<FactorDetailUiModel>> =
        factorRepository.getFactorDetailUiWithAggregatedDiscounts(factorId).asLiveData()

    fun getFactorItemCount(factorId: Int): LiveData<Int> =
        factorRepository.getFactorItemCount(factorId)

    fun getFactorDetails(factorId: Int): LiveData<List<FactorDetailEntity>> {
        return factorRepository.getFactorDetails(factorId).asLiveData()
    }

    val productInputCache =
        mutableMapOf<Int, Pair<Double, Double>>()

    private fun updateSendingState(factorId: Int, sending: Boolean) {
        val updated = _allHeaders.value?.map {
            if (it.factorId == factorId)
                it.copy(isSending = sending)
            else it
        } ?: return

        _allHeaders.value = updated
        applyFilter(lastQuery)
    }

    private val _sendFactorResult =
        MutableLiveData<Event<NetworkResult<Int>>>()

    val sendFactorResult: LiveData<Event<NetworkResult<Int>>> =
        _sendFactorResult

    fun sendFactor(factorId: Int) {
        viewModelScope.launch {

            updateSendingState(factorId, true)
            _sendFactorResult.value = Event(NetworkResult.Loading)

            val request = buildFinalFactorRequest(factorId)
            val body = listOf(request)
            Log.d("FINAL_json1", body.toString())

            when (val result = factorRepository.sendFactorToServer(body)) {

                is NetworkResult.Success -> {
                    factorRepository.deleteFactor(factorId)
                    _sendFactorResult.value = Event(
                        NetworkResult.Success(factorId, result.message)
                    )
                    Log.d("FINAL_json2", "ok")
                }

                is NetworkResult.Error -> {
                    updateSendingState(factorId, false)
                    _sendFactorResult.value = Event(NetworkResult.Error(result.message))
                }

                else -> {}
            }
        }
    }

    private suspend fun buildFinalFactorRequest(factorId: Int): FinalFactorRequestDto {
        val header = factorRepository.getHeaderByLocalId(factorId.toLong())
            ?: throw IllegalStateException("Header not found")

        val details = factorRepository.getAllFactorDetails(header.id).first()
        val gifts = factorRepository.getFactorGifts(header.id)
        // دریافت تخفیف‌های سطح فاکتور (با factorDetailId = null)
        val factorLevelDiscounts = factorRepository.getFactorDiscounts(header.id, null)

        Log.d("FINAL_header", header.id.toString())
        Log.d("FINAL_details", details.toString())
        Log.d("FINAL_gifts", gifts.toString())


        val finalDetails = details.map { d ->
            // تغییر این خط به صورت suspend
            val detailDiscounts = if (d.id != null) {
                factorRepository.getFactorDiscounts(header.id, d.id)
            } else {
                emptyList()
            }
            FinalFactorDetailDto(
                id = d.id,
                factorId = header.id,
                sortCode = d.sortCode ?: 1,
                anbarId = header.defaultAnbarId ?: 0,
                productId = d.productId,
                actId = header.actId,
                unit1Value = d.unit1Value,
                unit2Value = d.unit2Value,
                price = d.price,
                packingId = d.packingId,
                packingValue = d.packingValue,
                vat = d.vat,
                productSerial = d.productSerial ?: 0,
                isGift = d.isGift,
                returnCauseId = d.returnCauseId,
                isCanceled = d.isCanceled,
                isModified = d.isModified,
                description = d.description,
                unit1Rate = d.unit1Rate,
                factorDiscounts = detailDiscounts.map { dis ->
                    FinalFactorDiscountDto(
                        sortCode = dis.sortCode,
                        discountId = dis.discountId,
                        price = dis.price,
                        discountPercent = dis.discountPercent
                    )
                }
            )
        }

        // تبدیل تخفیف‌های سطح فاکتور به DTO
        val finalDiscounts = factorLevelDiscounts.map { dis ->
            FinalFactorDiscountDto(
                sortCode = dis.sortCode,
                discountId = dis.discountId,
                price = dis.price,
                discountPercent = dis.discountPercent
            )
        }
        val finalGifts = gifts.map { g ->
            FinalFactorGiftDto(
                productId = g.productId,
                discountId = g.discountId,
                price = g.price,
            )
        }

        return FinalFactorRequestDto(
            id = header.id,
            uniqueId = header.uniqueId,
            formKind = header.formKind ?: 0,
            centerId = header.centerId ?: 0,
            createDate = header.createDate,
            persianDate = header.persianDate,
            invoiceCategoryId = header.invoiceCategoryId ?: 0,
            patternId = header.patternId ?: 0,
            dueDate = header.dueDate,
            deliveryDate = header.deliveryDate,
            createTime = header.createTime,
            customerId = header.customerId,
            directionDetailId = header.directionDetailId ?: 0,
            visitorId = header.visitorId ?: 0,
            distributorId = header.distributorId,
            description = header.description,
            sabt = header.sabt,
            createUserId = header.createUserId ?: 0,
            saleCenterId = header.saleCenterId ?: 0,
            actId = header.actId ?: 0,
            recipientId = header.recipientId,
            settlementKind = header.settlementKind,
            latitude = header.latitude ?: 0.0,
            longitude = header.longitude ?: 0.0,
            defaultAnbarId = header.defaultAnbarId ?: 0,
            factorDetails = finalDetails,
            factorDiscounts = finalDiscounts,
            factorGiftInfos = finalGifts
        )
    }

    fun getMaxFactorDetailId(): LiveData<Int> {
        return factorRepository.getMaxFactorDetailId()
    }


    fun getCount(): LiveData<Int> {
        return factorRepository.getCount()
    }

    fun getFactorDetailByFactorIdAndProductId(
        factorId: Int,
        productId: Int
    ): LiveData<FactorDetailEntity> {
        return factorRepository.getFactorDetailByFactorIdAndProductId(factorId, productId)
            .asLiveData()
    }

    suspend fun getExistingFactorDetail(factorId: Int, productId: Int): FactorDetailEntity? {
        return factorRepository.getFactorDetailByFactorIdAndProductIdAsFlow(factorId, productId)
            .firstOrNull()
    }

    suspend fun calculateDiscountInsert(
        applyKind: Int,
        factorHeader: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?
    ) =
        discountRepository.calculateDiscountInsert(
            applyKind = applyKind,
            factorHeader = factorHeader,
            factorDetail = factorDetail
        )

    private val _productSavingState = MutableStateFlow(false)
    val isProductSaving: StateFlow<Boolean> = _productSavingState.asStateFlow()

    // متد شروع ذخیره‌سازی
    fun startProductSaving() {
        _productSavingState.value = true
    }

    // متد انتظار برای اتمام
    suspend fun waitForProductSavingComplete() {
        // حداکثر 2 ثانیه انتظار (برای جلوگیری از هنگ شدن)
        withTimeoutOrNull(1000) {
            snapshotFlow { _productSavingState.value }
                .first { !it }
        }
    }

    suspend fun saveProductWithDiscounts(
        detail: FactorDetailEntity,
        factorHeader: FactorHeaderEntity,
        vatPercent: Double,
        tollPercent: Double
    ) = withContext(Dispatchers.IO) {
        try {
            appDatabase.withTransaction {

                //  ذخیره اولیه ردیف (بدون VAT صحیح)
                val savedDetailId = factorRepository.addOrUpdateDetail(detail)
                val savedDetail = detail.copy(id = savedDetailId)

                // اعمال تخفیف‌ها - این مرحله FactorDiscountها را ایجاد می‌کند
                discountRepository.calculateDiscountInsert(
                    applyKind = DiscountApplyKind.ProductLevel.ordinal,
                    factorHeader = factorHeader,
                    factorDetail = savedDetail
                )

                // 3. محاسبه مقادیر

                val totalDiscount = factorRepository.getTotalDiscountForDetail(savedDetail.id)
                val totalAddition = factorRepository.getTotalAdditionForDetail(savedDetail.id)

                // 4. محاسبه قیمت پس از تخفیف
                val priceAfterDiscount =
                    Math.round(savedDetail.price - totalDiscount + totalAddition)

                val vat = Math.round(vatPercent * priceAfterDiscount).toDouble()
                val toll = Math.round(tollPercent * priceAfterDiscount).toDouble()


                // 4. فقط VAT را آپدیت کنید - سایر فیلدها (مثل sortCode) دست نخورده باقی می‌مانند
                factorRepository.updateVatForDetail(savedDetailId, vat)

                factorRepository.updateHasDetail(
                    factorHeader.id,
                    true
                )
            }
        } finally {
            // اتمام عملیات - اینجا تمام تغییرات یکجا کامیت شده‌اند
            _productSavingState.value = false
        }
    }

    private val _operationResult = MutableSharedFlow<Boolean>()
    val operationResult = _operationResult.asSharedFlow()


    suspend fun removeGiftsAndDiscounts(factorId: Int) {
        discountRepository.removeGiftsAndAutoDiscounts(factorId)
    }

    // جمع کل تخفیف‌ها (سطوح ردیف + فاکتور)
    suspend fun getTotalDiscountForFactor(factorId: Int): Double {
        return withContext(Dispatchers.IO) {
            // جمع تخفیف‌های سطح ردیف
            val productLevelDiscount =
                factorRepository.getTotalProductLevelDiscount(factorId) ?: 0.0

            // جمع تخفیف‌های سطح فاکتور (فقط ردیف‌هایی که factorDetailId = NULL دارند)
            val factorLevelDiscount = factorRepository.getTotalFactorLevelDiscount(factorId) ?: 0.0

            productLevelDiscount + factorLevelDiscount
        }
    }

    // حذف فقط تخفیف‌های سطح فاکتور (بدون تأثیر بر تخفیف‌های ردیف)
    suspend fun removeFactorLevelDiscounts(factorId: Int) {
        withContext(Dispatchers.IO) {
            factorRepository.deleteFactorLevelDiscounts(factorId)
        }
    }

    suspend fun deleteFactorDetailWithSabtCheck(
        detail: FactorDetailUiModel,
        factorId: Int,
        currentSabt: Int
    ) {
        // اگر سفارش تکمیل شده (sabt=1)، ابتدا آن را به حالت پیش‌نویس برگردان
        if (currentSabt == 1) {
            // 1. غیرفعال کردن حالت تکمیل
            updateHeader(sabt = 0)
            factorHeader.value?.let { header ->
                updateFactorHeader(header.copy(sabt = 0))
            }

            // 2. حذف تخفیف‌های سطح فاکتور و هدایا
            removeGiftsAndDiscounts(factorId)
            markDiscountRemoved()
        }

        // 3. حذف ردیف (همیشه انجام شود)
        deleteFactorDetail(detail)
    }

    fun updateSabtFromOfflineList(
        factorId: Int,
        sabt: Int
    ) = viewModelScope.launch {

        val header = getFactorHeaderById(factorId) ?: return@launch

        if (sabt == 1) {
            // ثبت سفارش
            calculateDiscountInsert(
                applyKind = DiscountApplyKind.FactorLevel.ordinal,
                factorHeader = header,
                factorDetail = null
            )
        } else {
            // لغو ثبت
            removeGiftsAndDiscounts(factorId)
        }


        val details = factorRepository.getFactorDetailsRaw(factorId)
        var sumPrice = 0.0
        var sumVat = 0.0

        for (item in details) {
            sumPrice += item.unit1Rate * item.unit1Value
            sumVat += item.vat
        }

        val totalDiscount = getTotalDiscountForFactor(factorId)

        val finalPrice = (sumPrice - totalDiscount) + sumVat
        Log.d("OfflineListsumPrice", sumPrice.toString())
        Log.d("OfflineListsumVat", sumVat.toString())
        Log.d("OfflineListtotalDiscount", totalDiscount.toString())
        Log.d("OfflineListfinalPrice", finalPrice.toString())

        updateFactorHeader(
            header.copy(
                sabt = sabt,
                finalPrice = finalPrice
            )
        )

        // لیست آفلاین رو رفرش کن
      //  loadOfflineHeaders()
    }




}
