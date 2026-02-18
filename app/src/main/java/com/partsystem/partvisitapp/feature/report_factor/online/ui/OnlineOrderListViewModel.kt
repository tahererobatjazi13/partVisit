package com.partsystem.partvisitapp.feature.report_factor.online.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.toEnglishDigits
import com.partsystem.partvisitapp.feature.report_factor.online.repository.OnlineOrderListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineOrderListViewModel @Inject constructor(
    private val onlineOrderListRepository: OnlineOrderListRepository
) : ViewModel() {

    private val _visitorList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorVisitorList: LiveData<NetworkResult<List<ReportFactorDto>>> = _visitorList

    private val _customerList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorCustomerList: LiveData<NetworkResult<List<ReportFactorDto>>> = _customerList

    private var originalVisitor = emptyList<ReportFactorDto>()
    private var originalCustomer = emptyList<ReportFactorDto>()

    fun fetchReportFactorVisitorList(type: Int, visitorId: Int) = viewModelScope.launch {
        _visitorList.value = NetworkResult.Loading
        when (val result = onlineOrderListRepository.getReportFactorVisitor(type, visitorId)) {
            is NetworkResult.Success -> {
                originalVisitor = result.data
                _visitorList.value = result
            }

            else -> _visitorList.value = result
        }
    }

    fun fetchReportFactorCustomerList(type: Int, customerId: Int) = viewModelScope.launch {
        _customerList.value = NetworkResult.Loading
        when (val result = onlineOrderListRepository.getReportFactorCustomer(type, customerId)) {
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
        _reportFactorDetail.value = onlineOrderListRepository.getReportFactorDetail(type, factorId)
    }

    // یک تابع اکستنشن برای فیلتر تمیز
    private fun ReportFactorDto.matchesQuery(q: String): Boolean =
        customerName.contains(q, ignoreCase = true) || id.toString().contains(q)


}