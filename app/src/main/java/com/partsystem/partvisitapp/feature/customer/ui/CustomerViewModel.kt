package com.partsystem.partvisitapp.feature.customer.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.feature.customer.repository.CustomerRepository
import com.partsystem.partvisitapp.feature.report_factor.online.model.DirectionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val mainPreferences: MainPreferences
) : ViewModel() {

    private val _customers = MutableLiveData<List<CustomerEntity>>()  // لیست کامل

    private val _filteredCustomers = MutableLiveData<List<CustomerEntity>>() // لیست فیلتر شده
    val filteredCustomers: LiveData<List<CustomerEntity>> get() = _filteredCustomers

    //  با برنامه ویزیت (با تاریخ)
    fun loadCustomersWithSchedule(persianDate: String) {
        viewModelScope.launch {
            val saleCenterId = mainPreferences.saleCenterId.first() ?: 0
            val visitorId = mainPreferences.personnelId.first() ?: 0

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
            val saleCenterId = mainPreferences.saleCenterId.first() ?: 0
            val visitorId = mainPreferences.personnelId.first() ?: 0

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


    // ---------------------- CustomerDirection List ----------------------
    private val _allDirections = MutableStateFlow<List<DirectionModel>>(emptyList())
    private val _filteredDirections = MutableStateFlow<List<DirectionModel>>(emptyList())
    val filteredDirections: StateFlow<List<DirectionModel>> = _filteredDirections

    init {
        viewModelScope.launch {
            repository.getAllCustomerDirections().collect { list ->
                _allDirections.value = list
                _filteredDirections.value = list
            }
        }
    }

    fun filterCustomerDirections(query: String) {
        val base = _allDirections.value
        _filteredDirections.value =
            if (query.isBlank()) base
            else base.filter { it.directionName.contains(query.trim(), ignoreCase = true) }
    }

    suspend fun ensureDirectionsLoaded() {
        if (_allDirections.value.isNotEmpty()) return

        repository.getAllCustomerDirections().first().also { list ->
            _allDirections.value = list
            _filteredDirections.value = list
        }
    }

}
