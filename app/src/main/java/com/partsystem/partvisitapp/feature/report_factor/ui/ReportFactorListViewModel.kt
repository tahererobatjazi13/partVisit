package com.partsystem.partvisitapp.feature.report_factor.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.feature.report_factor.repository.ReportFactorListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportFactorListViewModel @Inject constructor(
    private val reportFactorListRepository: ReportFactorListRepository
) : ViewModel() {

    private val _reportFactors = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactors: LiveData<NetworkResult<List<ReportFactorDto>>> = _reportFactors

    /**
     * دریافت لیست سفارشات از Repository و به‌روزرسانی LiveData
     */
    fun fetchReportFactor(type: Int, visitorId: Int) = viewModelScope.launch {
        _reportFactors.value = NetworkResult.Loading
        _reportFactors.value = reportFactorListRepository.getReportFactorVisitor(type, visitorId)
    }

  /*  private val _orderDetail = MutableLiveData<NetworkResult<OrderDetail>>()
    val orderDetail: LiveData<NetworkResult<OrderDetail>> = _orderDetail

    *//**
     * دریافت لیست جزییات یک سفارش از Repository و به‌روزرسانی LiveData
     *//*
    fun fetchOrderDetail(id: Int) = viewModelScope.launch {
        _orderDetail.value = NetworkResult.Loading()
        _orderDetail.value = orderListRepository.getOrderDetail(id)
    }*/
}
