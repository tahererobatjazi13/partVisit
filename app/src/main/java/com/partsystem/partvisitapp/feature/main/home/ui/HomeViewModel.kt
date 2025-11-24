package com.partsystem.partvisitapp.feature.main.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.feature.main.home.model.HomeMenuItem
import com.partsystem.partvisitapp.feature.main.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _homeMenuItem = MutableLiveData<List<HomeMenuItem>>()
    val homeMenuItems: LiveData<List<HomeMenuItem>> get() = _homeMenuItem

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        _homeMenuItem.value = listOf(
            HomeMenuItem(1, R.string.label_product_catalog, R.drawable.ic_home_catalog),
            HomeMenuItem(2, R.string.label_product_group, R.drawable.ic_home_group_product),
            HomeMenuItem(3, R.string.label_register_order, R.drawable.ic_home_register_order),
            HomeMenuItem(4, R.string.label_customers, R.drawable.ic_home_customer),
            HomeMenuItem(5, R.string.label_reports, R.drawable.ic_home_report),
            HomeMenuItem(6, R.string.label_orders, R.drawable.ic_home_order),
            HomeMenuItem(7, R.string.label_logout, R.drawable.ic_home_exit),
            HomeMenuItem(8, R.string.label_setting, R.drawable.ic_home_setting),
        )
    }

    private val _applicationSetting =
        MutableLiveData<NetworkResult<List<ApplicationSettingEntity>>>()
    val applicationSetting: LiveData<NetworkResult<List<ApplicationSettingEntity>>> =
        _applicationSetting

    fun fetchApplicationSetting() = viewModelScope.launch {
        _applicationSetting.value = NetworkResult.Loading
        _applicationSetting.value = homeRepository.fetchAndSaveApplicationSetting()
    }

    private val _groupProducts = MutableLiveData<NetworkResult<List<GroupProductEntity>>>()
    val groupProducts: LiveData<NetworkResult<List<GroupProductEntity>>> = _groupProducts

    fun fetchGroupProducts() = viewModelScope.launch {
        _groupProducts.value = NetworkResult.Loading
        _groupProducts.value = homeRepository.fetchAndSaveGroups()
    }

    private val _products = MutableLiveData<NetworkResult<List<ProductEntity>>>()
    val products: LiveData<NetworkResult<List<ProductEntity>>> = _products

    fun fetchProducts() = viewModelScope.launch {
        _products.value = NetworkResult.Loading
        _products.value = homeRepository.fetchAndSaveProducts()
    }

    private val _productImages = MutableLiveData<NetworkResult<List<ProductImageEntity>>>()
    val productImages: LiveData<NetworkResult<List<ProductImageEntity>>> = _productImages

    fun fetchProductImages() = viewModelScope.launch {
        _productImages.value = NetworkResult.Loading
        _productImages.value = homeRepository.fetchAndSaveImages()
    }

    private val _productPacking = MutableLiveData<NetworkResult<List<ProductPackingEntity>>>()
    val productPacking: LiveData<NetworkResult<List<ProductPackingEntity>>> = _productPacking

    fun fetchProductPacking() = viewModelScope.launch {
        _productPacking.value = NetworkResult.Loading
        _productPacking.value = homeRepository.fetchAndSaveProductPacking()
    }
    private val _customers = MutableLiveData<NetworkResult<List<CustomerEntity>>>()
    val customers: LiveData<NetworkResult<List<CustomerEntity>>> = _customers

    fun fetchCustomers() = viewModelScope.launch {
        _customers.value = NetworkResult.Loading
        _customers.value = homeRepository.fetchAndSaveCustomers()
    }

    private val _customerDirections =
        MutableLiveData<NetworkResult<List<CustomerDirectionEntity>>>()
    val customerDirections: LiveData<NetworkResult<List<CustomerDirectionEntity>>> =
        _customerDirections

    fun fetchCustomerDirections() = viewModelScope.launch {
        _customerDirections.value = NetworkResult.Loading
        _customerDirections.value = homeRepository.fetchAndSaveCustomerDirections()
    }

    private val _invoiceCategory = MutableLiveData<NetworkResult<List<InvoiceCategoryEntity>>>()
    val invoiceCategory: LiveData<NetworkResult<List<InvoiceCategoryEntity>>> = _invoiceCategory

    fun fetchInvoiceCategory() = viewModelScope.launch {
        _invoiceCategory.value = NetworkResult.Loading
        _invoiceCategory.value = homeRepository.fetchAndSaveInvoiceCategory()
    }

    private val _pattern = MutableLiveData<NetworkResult<List<PatternEntity>>>()
    val pattern: LiveData<NetworkResult<List<PatternEntity>>> = _pattern

    fun fetchPattern() = viewModelScope.launch {
        _pattern.value = NetworkResult.Loading
        _pattern.value = homeRepository.fetchAndSavePattern()
    }

    private val _act = MutableLiveData<NetworkResult<List<ActEntity>>>()
    val act: LiveData<NetworkResult<List<ActEntity>>> = _act

    fun fetchAct() = viewModelScope.launch {
        _act.value = NetworkResult.Loading
        _act.value = homeRepository.fetchAndSaveAct()
    }
}


