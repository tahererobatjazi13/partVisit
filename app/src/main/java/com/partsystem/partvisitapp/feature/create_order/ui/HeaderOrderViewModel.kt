package com.partsystem.partvisitapp.feature.create_order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.feature.create_order.repository.HeaderOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class HeaderOrderViewModel @Inject constructor(
    private val repository: HeaderOrderRepository
) : ViewModel() {

    private val _currentFactor = MutableLiveData<FactorEntity>()
    val currentFactor: LiveData<FactorEntity> get() = _currentFactor

    fun setFactor(factor: FactorEntity) {
        _currentFactor.value = factor
    }

    fun updateFactor(factor: FactorEntity) = viewModelScope.launch {
        repository.update(factor)
        _currentFactor.postValue(factor)
    }

    fun insertFactor(factor: FactorEntity) = viewModelScope.launch {
        val id = repository.insert(factor)
        // factor.id = id.toInt()
        _currentFactor.postValue(factor)
    }

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

    fun getPattern(): LiveData<List<PatternEntity>> {
        return repository.getPattern().asLiveData()
    }

    fun getAct(): LiveData<List<ActEntity>> {
        return repository.getAct().asLiveData()
    }


    private val _patterns = MutableLiveData<List<PatternEntity>>()
    val patterns: LiveData<List<PatternEntity>> = _patterns

    fun loadAssignDirectionCustomerByCustomerId(customerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val directions = repository.getAssignDirectionCustomerByCustomerId(customerId)

            _currentFactor.value?.let { factor ->
                directions.forEach { item ->
                    if (item.isDistribution) factor.distributorId = item.tafsiliId
                    if (item.isDemands) factor.recipientId = item.tafsiliId
                }

                _currentFactor.postValue(factor)
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



    private val _acts = MutableLiveData<List<ActEntity>>()
    val acts: LiveData<List<ActEntity>> get() = _acts

    private val _selectedAct = MutableLiveData<ActEntity?>()
    val selectedAct: LiveData<ActEntity?> get() = _selectedAct

    fun loadActs(patternId: Int, actKind: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getActsByPatternId(patternId, actKind)
            _acts.postValue(result)
        }
    }

    fun loadActById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getActById(id)
            _selectedAct.postValue(result)
        }
    }

}
