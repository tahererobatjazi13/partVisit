package com.partsystem.partvisitapp.feature.create_order.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FactorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,

    private val factorRepository: FactorRepository,
    private val discountRepository: DiscountRepository,
    val productRepository: ProductRepository,
) : ViewModel() {

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
        }
    }

    fun deleteFactor(factorId: Int) = viewModelScope.launch {
        factorRepository.deleteFactor(factorId)
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
        sabt: Int? = null,

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
            sabt = sabt ?: current.sabt,

            )
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

    suspend fun updateFactorHeader(header: FactorHeaderEntity) =
        factorRepository.updateFactorHeader(header)

    ////////////

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
        factorRepository.getFactorDetailUi(factorId)

    fun getFactorItemCount(factorId: Int): LiveData<Int> =
        factorRepository.getFactorItemCount(factorId)

    fun getFactorDetails(factorId: Int): LiveData<List<FactorDetailEntity>> {
        return factorRepository.getFactorDetails(factorId).asLiveData()
    }

    fun getAllFactorDetails(): LiveData<List<FactorDetailEntity>> {
        return factorRepository.getAllFactorDetails().asLiveData()
    }

    fun getFactorDiscountsLive(
        factorId: Int,
        factorDetailId: Int
    ): LiveData<List<FactorDiscountEntity>> {
        return factorRepository.getFactorDiscountsLive(factorId, factorDetailId).asLiveData()
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

    fun sendFactor(factorId: Int, sabt: Int) {
        viewModelScope.launch {

            updateSendingState(factorId, true)
            _sendFactorResult.value = Event(NetworkResult.Loading)

            val request = buildFinalFactorRequest(factorId, sabt)
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

    private suspend fun buildFinalFactorRequest(factorId: Int, sabt: Int): FinalFactorRequestDto {
        val header = factorRepository.getHeaderByLocalId(factorId.toLong())
            ?: throw IllegalStateException("Header not found")

        val details = factorRepository.getAllFactorDetails(header.id).first()
        val gifts = factorRepository.getFactorGifts(header.id)

        Log.d("FINAL_header", header.id.toString())
        Log.d("FINAL_details", details.toString())
        Log.d("FINAL_gifts", gifts.toString())

        val finalDetails = details.map { d ->
            // تغییر این خط به صورت suspend
            val discounts = factorRepository.getFactorDiscounts(
                header.id,
                d.id
            ) // اطمینان از اینکه این متد suspend است

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
                factorDiscounts = discounts.map { dis ->
                    FinalFactorDiscountDto(
                        sortCode = dis.sortCode,
                        discountId = dis.discountId,
                        price = dis.price,
                        factorDetailId = d.id,
                        discountPercent = dis.discountPercent
                    )
                }
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
            sabt = sabt,
            createUserId = header.createUserId ?: 0,
            saleCenterId = header.saleCenterId ?: 0,
            actId = header.actId ?: 0,
            recipientId = header.recipientId,
            settlementKind = header.settlementKind,
            latitude = header.latitude ?: 0.0,
            longitude = header.longitude ?: 0.0,
            defaultAnbarId = header.defaultAnbarId ?: 0,
            factorDetails = finalDetails,
            factorGiftInfos = finalGifts
        )
    }

    fun onProductConfirmed(
        applyKind: Int,
        factorHeader: FactorHeaderEntity,
        factorDetail: FactorDetailEntity
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Insert or update factor detail
            // factorDao.insertFactorDetail(factorDetail)

            // Calculate product-level discount
            discountRepository.calculateDiscountInsert(
                applyKind = applyKind,
                factorHeader = factorHeader,
                factorDetail = factorDetail
            )

            // Recalculate totals
            // updateFactorTotals(factorId)
        }
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

//    private val _currentFactorId = MutableLiveData<Int>()
//    val currentFactorId: LiveData<Int> = _currentFactorId
//
//    fun setFactorId(id: Int) {
//        _currentFactorId.value = id
//    }


    fun addOrUpdateProduct(
        detail: FactorDetailEntity/*,
        productId: Int,
        actId: Int?,
        anbarId: Int?,
        unit1Value: Double,
        packingValue: Double,
        unit2Value: Double,
        price: Double,
        packingId: Int,
        vat: Double,
        unit1Rate: Double*/
    ) {
        Log.d("productdetailunit1Value11", detail.unit1Value.toString())

        // val factorId = _currentFactorId.value ?: return

        viewModelScope.launch {

            factorRepository.addOrUpdateDetail(
                detail
                /*    factorId = factorId,
                    productId = productId,
                    actId = actId,
                    anbarId = anbarId,
                    unit1Value = unit1Value,
                    packingValue = packingValue,
                    unit2Value = unit2Value,
                    price = price,
                    packingId = packingId,
                    vat = vat,
                    unit1Rate = unit1Rate,*/
            )
        }
    }

    /*
        suspend fun saveProductWithDiscounts(
            detail: FactorDetailEntity,
            factorHeader: FactorHeaderEntity,
            productRate: Double
        ) {
            // 1. ابتدا FactorDetail را ذخیره کن و id واقعی آن را بگیر
            val savedDetailId = factorRepository.addOrUpdateDetail(detail)

            // 2. حالا detail ذخیره شده — id آن معتبر است
            val savedDetail = detail.copy(id = savedDetailId)

            // 3. حالا می‌توانیم تخفیف را ذخیره کنیم
            discountRepository.calculateDiscountInsert(
                applyKind = DiscountApplyKind.ProductLevel.ordinal,
                factorHeader = factorHeader,
                factorDetail = savedDetail
            )
        }*/

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


    suspend fun saveProductWithDiscounts(
        detail: FactorDetailEntity,
        factorHeader: FactorHeaderEntity,
        productRate: Double,
        vatPercent: Double,
        tollPercent: Double
    ) = withContext(Dispatchers.IO) {

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
        val priceAfterDiscount = Math.round(savedDetail.price - totalDiscount + totalAddition)

        val vat = Math.round(vatPercent * priceAfterDiscount).toDouble()
        val toll = Math.round(tollPercent * priceAfterDiscount).toDouble()


        // 4. فقط VAT را آپدیت کنید - سایر فیلدها (مثل sortCode) دست نخورده باقی می‌مانند
        factorRepository.updateVatForDetail(savedDetailId, vat)
    }

    /*
        suspend fun saveProductWithDiscounts(
            detail: FactorDetailEntity,
            factorHeader: FactorHeaderEntity,
            productRate: Double,
            vatPercent: Double,
            tollPercent: Double
        ) = withContext(Dispatchers.IO) {
            // 1. ذخیره اولیه ردیف (بدون VAT صحیح)
            val savedDetailId = factorRepository.addOrUpdateDetail(detail)
            val savedDetail = detail.copy(id = savedDetailId)

            // 2. اعمال تخفیف‌ها - این مرحله FactorDiscountها را ایجاد می‌کند
            discountRepository.calculateDiscountInsert(
                applyKind = DiscountApplyKind.ProductLevel.ordinal,
                factorHeader = factorHeader,
                factorDetail = savedDetail
            )

            // 3. بازیابی مجموع تخفیف‌ها و اضافات از دیتابیس
            val totalDiscount = factorRepository.getTotalDiscountForDetail(savedDetail.id)
            val totalAddition = factorRepository.getTotalAdditionForDetail(savedDetail.id)

            // 4. محاسبه قیمت پس از تخفیف
            val priceAfterDiscount = Math.round(savedDetail.price - totalDiscount + totalAddition)


            val vat = Math.round(vatPercent * priceAfterDiscount).toDouble()
            val toll = Math.round(tollPercent * pri  ceAfterDiscount).toDouble()

            // 6. به‌روزرسانی نهایی ردیف با مقادیر صحیح
            val updatedDetail = savedDetail.copy(
                vat = vat
            )

            factorRepository.updateFactorDetail(updatedDetail)
        }
    */
    /*
        fun saveProductWithDiscounts(
            detail: FactorDetailEntity,
            factorHeader: FactorHeaderEntity,
            productRate: Double,
            hasExistingDetail: Boolean
        ) {
            viewModelScope.launch {
                // 1. به‌روزرسانی هدر (یک بار)
                if (!factorHeader.hasDetail) {
                    updateFactorHeader(factorHeader.copy(hasDetail = true))
                }

                // 2. ذخیره‌سازی ردیف (خودکار تشخیص اینزرت/آپدیت)
                addOrUpdateProduct(detail)

                // 3. محاسبه و ذخیره تخفیف‌ها
                onProductConfirmed(
                    applyKind = DiscountApplyKind.ProductLevel.ordinal,
                    factorHeader = factorHeader,
                    factorDetail = detail
                )

                // 4. نوتیفیکیشن موفقیت (اختیاری)
             //   _uiMessage.value = "محصول با موفقیت ${if (hasExistingDetail) "ویرایش" else "افزوده"} شد"
            }
        }*/


    private val _operationResult = MutableSharedFlow<Boolean>()
    val operationResult = _operationResult.asSharedFlow()

    fun removeGiftsAndDiscounts(factorId: Int) {
        viewModelScope.launch {
            val success = discountRepository.removeGiftsAndAutoDiscounts(factorId)
            //  _operationResult.emit(success)

            /*   if (success) {
                   // Notify UI to refresh factor data
                   // DataHolder.isDirty = true (if maintaining legacy pattern)
               }*/
        }
    }
}
