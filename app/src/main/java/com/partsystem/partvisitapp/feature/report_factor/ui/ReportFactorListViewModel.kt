package com.partsystem.partvisitapp.feature.report_factor.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.toEnglishDigits
import com.partsystem.partvisitapp.feature.report_factor.repository.ReportFactorListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportFactorListViewModel @Inject constructor(
    private val reportFactorListRepository: ReportFactorListRepository
) : ViewModel() {

    private val _visitorList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorVisitorList: LiveData<NetworkResult<List<ReportFactorDto>>> = _visitorList

    private val _customerList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorCustomerList: LiveData<NetworkResult<List<ReportFactorDto>>> = _customerList

    private var originalVisitor = emptyList<ReportFactorDto>()
    private var originalCustomer = emptyList<ReportFactorDto>()

    fun fetchReportFactorVisitorList(type: Int, visitorId: Int) = viewModelScope.launch {
        _visitorList.value = NetworkResult.Loading
        when (val result = reportFactorListRepository.getReportFactorVisitor(type, visitorId)) {
            is NetworkResult.Success -> {
                originalVisitor = result.data
                _visitorList.value = result
            }

            else -> _visitorList.value = result
        }
    }

    fun fetchReportFactorCustomerList(type: Int, customerId: Int) = viewModelScope.launch {
        _customerList.value = NetworkResult.Loading
        when (val result = reportFactorListRepository.getReportFactorCustomer(type, customerId)) {
            is NetworkResult.Success -> {
                originalCustomer = result.data
                _customerList.value = result
            }

            else -> _customerList.value = result
        }
    }

    fun searchVisitorList(query: String) {
        val q = query.trim().toEnglishDigits()
        if (q.isBlank()) {
            _visitorList.value = NetworkResult.Success(originalVisitor)
            return
        }
        _visitorList.value = NetworkResult.Success(
            originalVisitor.filter { it.matchesQuery(q) }
        )
    }

    fun searchCustomerList(query: String) {
        val q = query.trim().toEnglishDigits()
        if (q.isBlank()) {
            _customerList.value = NetworkResult.Success(originalCustomer)
            return
        }
        _customerList.value = NetworkResult.Success(
            originalCustomer.filter { it.matchesQuery(q) }
        )
    }


    private val _reportFactorDetail = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorDetail: LiveData<NetworkResult<List<ReportFactorDto>>> = _reportFactorDetail

    fun fetchReportFactorDetail(type: Int, factorId: Int) = viewModelScope.launch {
        _reportFactorDetail.value = NetworkResult.Loading
        _reportFactorDetail.value = reportFactorListRepository.getReportFactorDetail(type, factorId)
    }
}

// یک تابع اکستنشن برای فیلتر تمیز
private fun ReportFactorDto.matchesQuery(q: String): Boolean =
    customerName?.contains(q, ignoreCase = true) == true ||
            id?.toString()?.contains(q) == true

