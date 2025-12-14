package com.partsystem.partvisitapp.feature.create_order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.AssignDirectionCustomerEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.utils.Event
import com.partsystem.partvisitapp.core.utils.SaleRateKind
import com.partsystem.partvisitapp.feature.create_order.repository.HeaderOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class HeaderOrderViewModel @Inject constructor(
    private val repository: HeaderOrderRepository
) : ViewModel() {

    private val _currentFactor = MutableLiveData<FactorHeaderEntity>()
    val currentFactor: LiveData<FactorHeaderEntity> get() = _currentFactor

    fun setFactor(factor: FactorHeaderEntity) {
        _currentFactor.value = factor
    }

    fun updateFactor(factor: FactorHeaderEntity) = viewModelScope.launch {
        repository.update(factor)
        _currentFactor.postValue(factor)
    }

//    fun insertFactor(factor: FactorHeaderEntity) = viewModelScope.launch {
//        val id = repository.insert(factor)
//        // factor.id = id.toInt()
//        _currentFactor.postValue(factor)
//    }

    private val _customerDirections = MutableLiveData<List<CustomerDirectionEntity>>()
    val customerDirections: LiveData<List<CustomerDirectionEntity>> = _customerDirections

    fun getCustomerDirectionsByCustomer(customerId: Int): LiveData<List<CustomerDirectionEntity>> {
        return repository.getCustomerDirectionsByCustomer(customerId).asLiveData()
    }
    /*   fun getInvoiceCategory(customerId: Int): LiveData<List<InvoiceCategoryEntity>> {
           return repository.getInvoiceCategory(customerId).asLiveData()
       }*/

    fun getInvoiceCategory(userId: Int): LiveData<List<InvoiceCategoryEntity>> {
        return repository.getInvoiceCategory(userId).asLiveData()
    }

  /*  fun getPattern(): LiveData<List<PatternEntity>> {
        return repository.getPattern().asLiveData()
    }
*/
    fun getAct(): LiveData<List<ActEntity>> {
        return repository.getAct().asLiveData()
    }


    private val _pattern = MutableLiveData<PatternEntity?>()
    val pattern: LiveData<PatternEntity?> get() = _pattern

/*    fun loadPatternById(id: Int) {
        viewModelScope.launch {
            val result = repository.getPatternById(id)
            _pattern.postValue(result)
        }
    }*/



    fun getPatternById(id: Int): LiveData<PatternEntity> =
        repository.getPatternById(id)

    private val _patterns = MutableLiveData<List<PatternEntity>>()
    val patterns: LiveData<List<PatternEntity>> = _patterns

    private val _assignDirection = MutableLiveData<FactorHeaderEntity>()
    val assignDirection: LiveData<FactorHeaderEntity> get() = _assignDirection

    fun loadAssignDirectionCustomerByCustomerId(customerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val directions = repository.getAssignDirectionCustomerByCustomerId(customerId)
            _assignDirection.value?.let { data ->
                directions.forEach { item ->
                    if (item.isDistribution) data.distributorId = item.tafsiliId
                    if (item.isDemands) data.recipientId = item.tafsiliId
                }
                _assignDirection.postValue(data)
            }
        }
    }

    fun loadPatterns(
        customer: Int,
        centerId: Int?,
        invoiceCategoryId: Int?,
        settlementKind: Int,
        date: String
    ) = viewModelScope.launch(Dispatchers.IO) {

        val result = repository.getPatternsForCustomer(
            customer,
            centerId,
            invoiceCategoryId,
            settlementKind,
            date
        )
        _patterns.postValue(result)
    }

    private val _saleCenters = MutableLiveData<List<SaleCenterEntity>>()
    val saleCenters: LiveData<List<SaleCenterEntity>> get() = _saleCenters

    fun loadSaleCenters(invoiceCategoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val centers = repository.getSaleCenters(invoiceCategoryId)
            _saleCenters.postValue(centers)
        }
    }
    private val _validationEvent = MutableLiveData<Event<Unit>>()
    val validationEvent: LiveData<Event<Unit>> get() = _validationEvent


    private val _errorMessageRes = MutableLiveData<Int?>()
    val errorMessageRes: LiveData<Int?> get() = _errorMessageRes

    fun validateHeader(saleCenterId: Int?, factor: FactorHeaderEntity) {
        viewModelScope.launch {
            val sc = saleCenterId?.let { repository.getSaleCenter(it) }
            val rateKind = sc?.saleRateKind ?: SaleRateKind.None

            if (/*rateKind == SaleRateKind.Pattern && */factor.patternId == null) {
                _errorMessageRes.value = R.string.error_selecting_pattern_mandatory
                return@launch
            }

            if (/*rateKind != SaleRateKind.None &&*/ factor.actId == null) {
                _errorMessageRes.value = R.string.error_selecting_act_mandatory
                return@launch
            }

            if (factor.defaultAnbarId == null) {
                _errorMessageRes.value = R.string.error_there_not_default_warehouse_sales_center
                return@launch
            }
            _errorMessageRes.value = null
            _validationEvent.value = Event(Unit)

        }
    }

    private val _acts = MutableLiveData<List<ActEntity>>()
    val acts: LiveData<List<ActEntity>> get() = _acts

    fun loadActs(patternId: Int, actKind: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getActsByPatternId(patternId, actKind)
            _acts.postValue(result)
        }
    }

    private val _productActId = MutableLiveData<Int?>()
    val productActId: LiveData<Int?> get() = _productActId

    fun loadProductActId(patternId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getProductActId(patternId)
            _productActId.postValue(result)
        }
    }
    private val _addedAct = MutableLiveData<ActEntity?>()
    val addedAct: LiveData<ActEntity?> get() = _addedAct

    fun loadAct(actId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val act = repository.getActById(actId)
            _addedAct.postValue(act)
        }
    }



    private val _defaultAnbarId = MutableStateFlow<Int?>(null)
    val defaultAnbarId: StateFlow<Int?> get() = _defaultAnbarId

    // دریافت DefaultAnbarId بر اساس SaleCenterId
    fun fetchDefaultAnbarId(saleCenterId: Int) {
        viewModelScope.launch {
            val anbarId = repository.getActiveSaleCenterAnbar(saleCenterId)
            _defaultAnbarId.value = anbarId
        }
    }
/*
    fun createFactor(saleCenterId: Int, formKind: FactorFormKind, date: String) {
        viewModelScope.launch {
            val defaultAnbarId = repository.getActiveSaleCenterAnbar(saleCenterId)

            val factor = FactorEntity(
                saleCenterId = saleCenterId,
                defaultAnbarId = defaultAnbarId,
                formKind = formKind
            )

            val newId = factorRepository.insertFactor(factor)
            println("New Factor inserted with ID: $newId")
        }
    }
*/

}
