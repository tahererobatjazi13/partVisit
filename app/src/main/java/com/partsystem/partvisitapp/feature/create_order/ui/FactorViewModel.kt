package com.partsystem.partvisitapp.feature.create_order.ui

import android.util.Log.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.CalculateDiscount
import com.partsystem.partvisitapp.core.utils.extensions.getTodayGregorian
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.extensions.toEnglishDigits
import com.partsystem.partvisitapp.feature.create_order.repository.FactorRepository
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class FactorViewModel @Inject constructor(
    private val factorRepository: FactorRepository,
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

    val allFactorDetails: LiveData<List<FactorDetailEntity>> = factorRepository.getAllFactorDetail()

    fun addDetail(item: FactorDetailEntity) {
        val productId = item.productId ?: return
        if ((item.unit1Value ?: 0.0) > 0.0 || (item.packingValue ?: 0.0) > 0.0) {
            factorItems[productId] = item
        } else {
            factorItems.remove(productId)
        }

        _totalCount.value = factorItems.size

        viewModelScope.launch {
            // factorRepository.saveFactorDetails(item)
        }
    }

    fun deleteFactorDetail(item: FactorDetailEntity) {
        viewModelScope.launch {
            factorRepository.deleteFactorDetail(item.productId!!)
        }
    }

    // اضافه کردن تخفیف
    fun addGift(giftInfo: FactorGiftInfoEntity) {
        val list = factorGifts.value!!
        list.add(giftInfo)
        factorGifts.postValue(list)
    }

    /*    // ساخت JSON نهایی
        fun buildFactorRequest(): FinalFactorRequest {
            val h = factorHeader.value!!

            return FinalFactorRequest(
                uniqueId = h.uniqueId,
                id = h.id,
                formKind = h.formKind,
                centerId = h.centerId,
                code = h.code,
                createDate = h.createDate,
                patternId = h.patternId,
                dueDate = h.dueDate,
                saleCenterId = h.saleCenterId,
                customerId = h.customerId,
                visitorId = h.visitorId,
                description = h.description,
                sabt = h.sabt,
                createUserId = h.createUserId,
                actId = h.actId,
                settlementKind = h.settlementKind,
                deliveryDate = h.deliveryDate,
                createTime = h.createTime,
                directionDetailId = h.directionDetailId,
                latitude = h.latitude,
                longitude = h.longitude,
                factorDetails = factorDetails.value ?: emptyList(),
                factorGiftInfos = factorGifts.value ?: emptyList()
            )
        }*/

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
        hasDetail: Boolean? = null
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
            hasDetail = hasDetail ?: current.hasDetail
        )

    }

    fun setDefaultDates() {
        val todayGregorian = getTodayGregorian()
        val todayPersian = getTodayPersianDate()

        val current = factorHeader.value ?: FactorHeaderEntity()
        factorHeader.value = current.copy(
            createDate = current.createDate ?: todayGregorian,
            dueDate = current.dueDate ?: todayGregorian,
            deliveryDate = current.deliveryDate ?: todayGregorian,
            persianDate = current.persianDate ?: todayPersian
        )
    }

    /* fun updateHeader(
         invoiceCategoryId: Int? = factorHeader.value?.invoiceCategoryId
     ) {
         factorHeader.value = factorHeader.value?.copy(
             invoiceCategoryId = invoiceCategoryId
         )
     }*/

    suspend fun loadProduct(productId: Int, actId: Int): ProductWithPacking? {
        return productRepository.getProductByActId(productId, actId)
    }

    suspend fun saveFactorHeader(header: FactorHeaderEntity) =
        factorRepository.saveFactorHeader(header)

    suspend fun updateFactorHeader(header: FactorHeaderEntity) =
        factorRepository.updateFactorHeader(header)

    private val _selectedProducts =
        MutableLiveData<MutableList<FactorDetailEntity>>(mutableListOf())
    val selectedProducts: LiveData<MutableList<FactorDetailEntity>> = _selectedProducts

    /*    var header: FactorHeaderEntity? = null

        fun saveHeader(header: FactorHeaderEntity) {
            this.header = header
        }*/

    /*    fun addOrUpdateProduct(detail: FactorDetailEntity) {
            // اگر محصول تکراری بود، ویرایش شود
            val index = selectedProducts.indexOfFirst { it.productId == detail.productId }
            if (index >= 0) selectedProducts[index] = detail
            else selectedProducts.add(detail)
        }*/
    fun addOrUpdateProduct(detail: FactorDetailEntity) {
        val list = _selectedProducts.value ?: mutableListOf()

        val index = list.indexOfFirst { it.productId == detail.productId }
        if (index >= 0) list[index] = detail
        else list.add(detail)

        _selectedProducts.value = list
    }

    /*    suspend fun saveToLocal() {
            header?.let { hdr ->
                val headerId = factorRepository.saveFactorHeader(hdr).toInt()
                val mappedDetails = selectedProducts.map {
                    it.copy(factorId = headerId)
                }
                factorRepository.saveFactorDetails(mappedDetails)
            }
        }

        fun buildFinalJson(): JSONObject {
            val json = JSONObject()
            val hdr = header!!

            json.put("uniqueId", hdr.uniqueId)
            json.put("formKind", hdr.formKind)
            json.put("customerId", hdr.customerId)


            val detailArray = JSONArray()
            selectedProducts.forEach { d ->
                val item = JSONObject()
                item.put("productId", d.productId)
                item.put("unit1Value", d.unit1Value)
                item.put("price", d.price)
                detailArray.put(item)
            }

            json.put("factorDetails", detailArray)
            return json
        }*/


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

    suspend fun saveHeaderAndGetId(header: FactorHeaderEntity): Long {
        return factorRepository.saveFactorHeader(header)
    }

    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity> =
        factorRepository.getHeaderById(id)


    private val _allHeaders = MutableLiveData<List<FactorHeaderUiModel>>()
    private val _filteredHeaders = MutableLiveData<List<FactorHeaderUiModel>>()
    val filteredHeaders: LiveData<List<FactorHeaderUiModel>> = _filteredHeaders

    init {
        viewModelScope.launch {
            factorRepository.getAllHeaderUi().collectLatest { list ->
                _allHeaders.value = list
                _filteredHeaders.value = list
            }
        }
    }

    fun filterHeaders(query: String) {
        val q = query
            .trim()
            .toEnglishDigits()

        val list = _allHeaders.value ?: emptyList()

        _filteredHeaders.value =
            if (q.isEmpty()) {
                list
            } else {
                list.filter { item ->
                    item.factorId.toString().contains(q) ||
                            item.customerName?.contains(q, ignoreCase = true) == true ||
                            item.patternName?.contains(q, ignoreCase = true) == true
                }
            }
    }


    // current draft uniqueId
    var currentUniqueId: String? = null
        private set

    /*    // create a new draft header
        fun createDraftHeader() {
            viewModelScope.launch(Dispatchers.IO) {

                val localId = factorRepository.createHeader(FactorHeaderEntity())
                val saved = factorRepository.getHeaderByLocalId(localId)
                currentUniqueId = saved?.uniqueId
                _currentHeader.postValue(saved)
                _details.postValue(emptyList())
                _discounts.postValue(emptyList())
            }
        }*/

    /* fun createDraftHeader() {
         viewModelScope.launch(Dispatchers.IO) {
             val localId = factorRepository.createHeader(FactorHeaderEntity())
             headerId = localId   // این خط لازم است!

             val saved = factorRepository.getHeaderByLocalId(localId)
             currentUniqueId = saved?.uniqueId
             _currentHeader.postValue(saved)

             _details.postValue(emptyList())
             _discounts.postValue(emptyList())
         }
     }*/


    /*    fun loadHeader(id: Long) {
                viewModelScope.launch(Dispatchers.IO) {
                    headerId = id
                    _header.postValue(factorRepository.getHeader(id))
                    _details.postValue(factorRepository.getFactorDetails(id))
                    _gifts.postValue(factorRepository.getFactorGifts(id))
                }
            }*/

    /*  fun updateHeaderLocal(upd: FactorHeaderEntity) {
          viewModelScope.launch(Dispatchers.IO) {
              factorRepository.updateHeader(upd)
              _header.postValue(factorRepository.getHeader(upd.id))
          }
      }*/

    /*   fun addDetailLocal(detail: FactorDetailEntity) {
           viewModelScope.launch(Dispatchers.IO) {
               factorRepository.addDetail(detail)
               _details.postValue(factorRepository.getDetails(detail.factorId))
           }
       }

       fun addGiftLocal(gift: FactorGiftInfoEntity) {
           viewModelScope.launch(Dispatchers.IO) {
               factorRepository.addGift(gift)
               _gifts.postValue(factorRepository.getGifts(gift.factorId))
           }
       }*/

    /*
            suspend fun buildFinalRequest(): FinalFactorRequest {
                // اجرا در coroutine caller (مثلاً lifecycleScope)
                val h = factorRepository.getHeader(headerId) ?: throw IllegalStateException("Header not found")
                val d = factorRepository.getDetails(headerId)
                val g = factorRepository.getGifts(headerId)

                val factorDetails = d.map { temp ->
                    FactorDetailEntity(
                        factorId = temp.factorId,
                        id = temp.id,
                        sortCode = 1,
                        anbarId = 6,
                        productId = temp.productId,
                        actId = h.actId,
                        unit1Value = temp.unit1Value,
                        unit2Value = temp.unit2Value,
                        price = temp.price,
                        description = temp.description,
                        packingId = temp.packingId,
                        packingValue = temp.packingValue,
                        vat = temp.vat,
                        productSerial = 0,
                        isGift = 0,
                        returnCauseId = 0,
                        isCanceled = 0,
                        isModified = 0,
                        unit1Rate = temp.unit1Rate,
                       // factorDiscounts = emptyList() // در این نسخه، تخفیفات هر detail را جدا نگه می‌داریم
                    )
                }

                val factorGifts = g.map { temp ->
                    FactorGiftInfoEntity(
                        id = temp.id,
                        factorId = temp.factorId,
                        discountId = temp.discountId,
                        productId = temp.productId,
                        price = temp.price,
                        arzPrice = 0.0
                    )
                }

                return FinalFactorRequest(
                    uniqueId = h.uniqueId,
                    id = h.id,
                    formKind = 17,
                    centerId = 1,
                    code = h.code,
                    createDate = h.createDate,
                    invoiceCategoryId = h.invoiceCategoryId,
                    patternId = h.patternId,
                    dueDate = h.deliveryDate,
                    customerId = h.customerId,
                    visitorId = h.visitorId,
                    description = h.description,
                    sabt = h.sabt,
                    createUserId = h.createUserId,
                    saleCenterId = h.saleCenterId,
                    actId = h.actId,
                    settlementKind = h.settlementKind,
                    deliveryDate = h.deliveryDate,
                    createTime = h.createTime,
                    directionDetailId = h.directionDetailId,
                    latitude = h.latitude,
                    longitude = h.longitude,
                    factorDetails = factorDetails,
                    factorGiftInfos = factorGifts
                )
            }
    */

    /*  // ارسال به سرور
      fun sendFactorToServer(onResult: (success: Boolean, code: Int?) -> Unit) {
          viewModelScope.launch(Dispatchers.IO) {
              try {
                  val req = buildFinalRequest()
                  val resp = factorRepository.sendFactor(req)
                  onResult(resp.isSuccessful, resp.code())
                  if (resp.isSuccessful) {
                      // پاک کردن Temp header بعد از ارسال
                      factorRepository.deleteHeader(headerId)
                  }
              } catch (e: Exception) {
                  onResult(false, null)
              }
          }
      }*/

    fun addFactorDetail(detail: FactorDetailEntity) {
        viewModelScope.launch {
            factorRepository.saveFactorDetail(detail)
        }
    }

    /*   fun addOrUpdateFactorDetail(detail: FactorDetailEntity) {
           viewModelScope.launch {
               if (detail.unit1Value!! <= 0 && detail.packingValue!! <= 0) {
                   factorRepository.deleteFactorDetail(
                       factorId = detail.factorId,
                       productId = detail.productId!!
                   )
                   factorItems.remove(detail.productId!!)
               } else {
                   factorRepository.upsertFactorDetail(detail)
                   factorItems[detail.productId!!] = detail

               }
               _totalCount.value = factorItems.size
           }
       }*/
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

    fun clearCart(factorId: Int) {
        viewModelScope.launch {
            factorRepository.clearFactor(factorId)
        }
    }

    fun getFactorItemCount(factorId: Int): LiveData<Int> =
        factorRepository.getFactorItemCount(factorId)

    fun getFactorDetails(factorId: Int): LiveData<List<FactorDetailEntity>> {
        return factorRepository.getFactorDetails(factorId).asLiveData()
    }

    fun updateByPacking(
        detail: FactorDetailEntity,
        packingValue: Double,
        product: ProductWithPacking,
        packing: ProductPackingEntity
    ) {
        viewModelScope.launch {

            val calculator = CalculateDiscount(productRepository)

            val values = calculator.fillProductValues(
                anbarId = factorHeader.value?.defaultAnbarId,
                product = product.product,
                packing = packing,
                unit1ValueInput = null,
                unit2ValueInput = null,
                packingValueInput = packingValue,
                isInput = false
            )

            val updated = detail.copy(
                unit1Value = values.unit1Value,
                unit2Value = values.unit2Value,
                packingValue = packingValue,
                packingId = packing.packingId,
                //  packingName = packing.packingName ?: ""
            )

            factorRepository.insertOrUpdateFactorDetail(updated)
        }
    }

    fun updateByUnit(
        detail: FactorDetailEntity,
        unit1Value: Double,
        product: ProductWithPacking,
        packing: ProductPackingEntity
    ) {
        viewModelScope.launch {

            val calculator = CalculateDiscount(productRepository)

            val values = calculator.fillProductValues(
                anbarId = factorHeader.value?.defaultAnbarId,
                product = product.product,
                packing = packing,
                unit1ValueInput = unit1Value,
                unit2ValueInput = null,
                packingValueInput = null,
                isInput = false
            )
            d("factorViewModelvalues", values.toString())

            val updated = detail.copy(
                unit1Value = unit1Value,
                unit2Value = values.unit2Value,
                packingValue = values.packingValue
            )
            d("factorViewModelupdated", updated.toString())
            factorRepository.insertOrUpdateFactorDetail(updated)
        }

    }


    val productInputCache =
        mutableMapOf<Int, Pair<Double, Double>>()


    fun getTotalPriceForHeader(factorId: Int): LiveData<Double> {
        val result = MutableLiveData<Double>()
        getFactorDetailUi(factorId).observeForever { details ->
            val total = details.sumOf { it.unit1Rate * it.unit1Value }
            result.value = total
        }
        return result
    }

}
