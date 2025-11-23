package com.partsystem.partvisitapp.feature.customer.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.feature.customer.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    // لیست اصلی مشتریان از دیتابیس
    private val _customerList = MutableLiveData<List<CustomerEntity>>()

    // لیست فیلترشده
    private val _filteredCustomerList = MutableLiveData<List<CustomerEntity>>()
    val customerList: LiveData<List<CustomerEntity>> get() = _filteredCustomerList

    init {
        // جمع‌آوری لیست مشتریان از Room و مقداردهی اولیه
        viewModelScope.launch {
            repository.getAllCustomers().collectLatest { list ->
                _customerList.value = list
                _filteredCustomerList.value = list
            }
        }
    }

    // فیلتر کردن محصولات بر اساس query
    fun filterCustomers(query: String) {
        val list = _customerList.value ?: emptyList()
        _filteredCustomerList.value = if (query.isEmpty()) {
            list
        } else {
            list.filter { it.name?.contains(query, ignoreCase = true) == true }
        }
    }

    // گرفتن مشتری با id مشخص
    fun getCustomerById(id: Int): LiveData<CustomerEntity> = repository.getCustomerById(id)
}
