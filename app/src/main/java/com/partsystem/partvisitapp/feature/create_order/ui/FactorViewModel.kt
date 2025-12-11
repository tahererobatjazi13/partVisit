package com.partsystem.partvisitapp.feature.create_order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.FinalFactorRequest
import com.partsystem.partvisitapp.feature.create_order.repository.HeaderOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.repository.FactorRepository
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@HiltViewModel
class FactorViewModel @Inject constructor(
    private val repository: HeaderOrderRepository,
    private val factorRepository: FactorRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {
    val factorHeader = MutableLiveData(FactorHeaderEntity())
    val factorDetails = MutableLiveData<MutableList<FactorDetailEntity>>(mutableListOf())
    val factorGifts = MutableLiveData<MutableList<FactorGiftInfoEntity>>(mutableListOf())

    private val factorItems = mutableMapOf<Int, FactorDetailEntity>()

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

    // ساخت JSON نهایی
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
    }

    suspend fun loadProduct(productId: Int, actId: Int): ProductWithPacking? {
        return productRepository.getProductByActId(productId, actId)
    }

    suspend fun saveFactorHeader(header: FactorHeaderEntity) =
        factorRepository.saveFactorHeader(header)


    private val _selectedProducts = MutableLiveData<MutableList<FactorDetailEntity>>(mutableListOf())
    val selectedProducts: LiveData<MutableList<FactorDetailEntity>> = _selectedProducts

    var header: FactorHeaderEntity? = null

    fun saveHeader(header: FactorHeaderEntity) {
        this.header = header
    }

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
}
