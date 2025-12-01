package com.partsystem.partvisitapp.feature.customer.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.feature.customer.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _customers = MutableLiveData<List<CustomerEntity>>()  // لیست کامل

    private val _filteredCustomers = MutableLiveData<List<CustomerEntity>>() // لیست فیلتر شده
    val filteredCustomers: LiveData<List<CustomerEntity>> get() = _filteredCustomers

    //  با برنامه ویزیت (با تاریخ)
    fun loadCustomersWithSchedule(persianDate: String) {
        viewModelScope.launch {
            val saleCenterId = userPreferences.saleCenterId.first() ?: 0
            val visitorId = userPreferences.personnelId.first() ?: 0

            if (saleCenterId == 0 || visitorId == 0) {
                _customers.postValue(emptyList())
                _filteredCustomers.postValue(emptyList())
                return@launch
            }

            repository.getCustomersBySchedule(saleCenterId, visitorId, persianDate)
                .collectLatest { list ->
                    _customers.postValue(list)
                    _filteredCustomers.postValue(list)
                }
        }
    }

    // بدون برنامه ویزیت
    fun loadCustomersWithoutSchedule() {
        viewModelScope.launch {
            val saleCenterId = userPreferences.saleCenterId.first() ?: 0
            val visitorId = userPreferences.personnelId.first() ?: 0

            if (saleCenterId == 0 || visitorId == 0) {
                _customers.postValue(emptyList())
                _filteredCustomers.postValue(emptyList())
                return@launch
            }

            repository.getCustomersWithoutSchedule(saleCenterId, visitorId)
                .collectLatest { list ->
                    _customers.postValue(list)
                    _filteredCustomers.postValue(list)
                }
        }
    }

    // فیلتر مشتری
    fun filterCustomers(query: String) {
        val list = _customers.value ?: emptyList()
        _filteredCustomers.value =
            if (query.isBlank()) list
            else list.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    fun getCustomerById(id: Int): LiveData<CustomerEntity> =
        repository.getCustomerById(id)
}
