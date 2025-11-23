package com.partsystem.partvisitapp.feature.create_order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.feature.create_order.repository.HeaderOrderRepository

import javax.inject.Inject

@HiltViewModel
class HeaderOrderViewModel @Inject constructor(
    private val repository: HeaderOrderRepository
) : ViewModel() {

    fun getCustomerDirections(customerId: Int): LiveData<List<CustomerDirectionEntity>> {
        return repository.getDirectionsByCustomer(customerId).asLiveData()
    }

    fun getInvoiceCategory(): LiveData<List<InvoiceCategoryEntity>> {
        return repository.getInvoiceCategory().asLiveData()
    }

    fun getPattern(): LiveData<List<PatternEntity>> {
        return repository.getPattern().asLiveData()
    }

    fun getAct(): LiveData<List<ActEntity>> {
        return repository.getAct().asLiveData()
    }
}
