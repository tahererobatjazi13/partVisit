package com.partsystem.partvisitapp.feature.customer.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.feature.customer.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _customers = MutableLiveData<List<CustomerEntity>>()  // لیست کامل مشتریان از کوئری
    val customers: LiveData<List<CustomerEntity>> get() = _customers

    private val _filteredCustomers = MutableLiveData<List<CustomerEntity>>() // فیلتر شده
    val filteredCustomers: LiveData<List<CustomerEntity>> get() = _filteredCustomers

    init {
        loadCustomersFromPreferences()
    }

    private fun loadCustomersFromPreferences() {
        viewModelScope.launch {

            combine(
                userPreferences.saleCenterId,
                userPreferences.personnelId
            ) { saleCenterId, visitorId ->
                Pair(saleCenterId, visitorId)
            }.collectLatest { (saleCenterId, visitorId) ->

                if (saleCenterId == 0 || visitorId == 0) {
                    _customers.postValue(emptyList())
                    _filteredCustomers.postValue(emptyList())
                    return@collectLatest
                }

                repository.getCustomers(saleCenterId!!, visitorId!!)
                    .collectLatest { list ->
                        _customers.postValue(list)
                        _filteredCustomers.postValue(list) // مقدار اولیه همان کل لیست
                    }
            }
        }
    }

    // فیلتر کردن مشتریان
    fun filterCustomers(query: String) {
        val list = _customers.value ?: emptyList()

        _filteredCustomers.value =
            if (query.isBlank()) {
                list
            } else {
                list.filter { customer ->
                    customer.name.contains(query.trim(), ignoreCase = true)
                }
            }
    }

    fun getCustomerById(id: Int): LiveData<CustomerEntity> =
        repository.getCustomerById(id)
}