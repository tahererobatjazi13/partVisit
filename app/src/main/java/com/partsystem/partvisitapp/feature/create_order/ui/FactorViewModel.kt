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
import com.partsystem.partvisitapp.core.database.entity.OrderEntity
import kotlinx.coroutines.launch

@HiltViewModel
class FactorViewModel @Inject constructor(
    private val repository: HeaderOrderRepository,
    private val factorRepository: FactorRepository,
) : ViewModel() {


    val factorHeader = MutableLiveData(FactorHeaderEntity())
    val factorDetails = MutableLiveData<MutableList<FactorDetailEntity>>(mutableListOf())
    val factorGifts = MutableLiveData<MutableList<FactorGiftInfoEntity>>(mutableListOf())

    private val factorItems = mutableMapOf<Int, FactorDetailEntity>()

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount


    val allFactorDetails: LiveData<List<FactorDetailEntity>> = factorRepository.getAllFactorDetail()

    fun addToCart(item: FactorDetailEntity) {
        val productId = item.productId ?: return
        if ((item.unit1Value ?: 0.0) > 0.0 || (item.packingValue ?: 0.0) > 0.0) {
            factorItems[productId] = item
        } else {
            factorItems.remove(productId)
        }

        _totalCount.value = factorItems.size

        viewModelScope.launch {
            factorRepository.insertFactorDetail(item)
        }
    }

    fun deleteFactorDetail(item: FactorDetailEntity) {
        viewModelScope.launch {
            factorRepository.deleteFactorDetail(item.productId!!)
        }
    }
    // اضافه کردن کالا
    fun addDetail(detail: FactorDetailEntity) {
        val list = factorDetails.value!!
        list.add(detail)
        factorDetails.postValue(list)
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
}
