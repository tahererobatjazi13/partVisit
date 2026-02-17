package com.partsystem.partvisitapp.feature.main.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity
import com.partsystem.partvisitapp.core.database.entity.AssignDirectionCustomerEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternDetailEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.database.entity.VatEntity
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleEntity
import com.partsystem.partvisitapp.core.database.entity.VisitorEntity
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.feature.main.home.model.HomeMenuItem
import com.partsystem.partvisitapp.feature.main.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val mainPreferences: MainPreferences

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

    // ----------- LiveData برای همه جداول ----------------
    val applicationSetting = MutableLiveData<NetworkResult<List<ApplicationSettingEntity>>>()
    val visitor = MutableLiveData<NetworkResult<List<VisitorEntity>>>()
    val visitSchedule = MutableLiveData<NetworkResult<List<VisitScheduleEntity>>>()
    val groupProducts = MutableLiveData<NetworkResult<List<GroupProductEntity>>>()
    val products = MutableLiveData<NetworkResult<List<ProductEntity>>>()
    val productImages = MutableLiveData<NetworkResult<List<ProductImageEntity>>>()
    val productPacking = MutableLiveData<NetworkResult<List<ProductPackingEntity>>>()
    val customers = MutableLiveData<NetworkResult<List<CustomerEntity>>>()
    val customerDirections = MutableLiveData<NetworkResult<List<CustomerDirectionEntity>>>()
    val assignDirectionCustomer =
        MutableLiveData<NetworkResult<List<AssignDirectionCustomerEntity>>>()
    val invoiceCategory = MutableLiveData<NetworkResult<List<InvoiceCategoryEntity>>>()
    val pattern = MutableLiveData<NetworkResult<List<PatternEntity>>>()
    val patternDetails = MutableLiveData<NetworkResult<List<PatternDetailEntity>>>()
    val act = MutableLiveData<NetworkResult<List<ActEntity>>>()
    val vat = MutableLiveData<NetworkResult<List<VatEntity>>>()
    val saleCenter = MutableLiveData<NetworkResult<List<SaleCenterEntity>>>()
    val discount = MutableLiveData<NetworkResult<List<DiscountEntity>>>()

    // ------------------- Fetch Functions -------------------
    fun fetchApplicationSetting() = viewModelScope.launch {
        applicationSetting.value = NetworkResult.Loading

        val result = homeRepository.fetchAndSaveApplicationSetting()
        applicationSetting.value = result

        // اگر موفق بود → مقدار ControlVisitSchedule را ذخیره کن
        if (result is NetworkResult.Success) {
            loadAndSaveControlVisitSchedule()
        }
    }

    private fun loadAndSaveControlVisitSchedule() {
        viewModelScope.launch {
            val value = homeRepository.getControlVisitSchedule()
            mainPreferences.saveControlVisitSchedule(value)
        }
    }

    fun fetchVisitors() = fetchTable(homeRepository::fetchAndSaveVisitors, visitor)
    fun fetchVisitSchedules() =
        fetchTable(homeRepository::fetchAndSaveVisitSchedules, visitSchedule)

    fun fetchGroupProducts() = fetchTable(homeRepository::fetchAndSaveGroups, groupProducts)
    fun fetchProducts() = fetchTable(homeRepository::fetchAndSaveProducts, products)

    suspend fun syncProducts() {
        products.postValue(NetworkResult.Loading)

        val result = homeRepository.fetchAndSaveProducts()
        products.postValue(result)

        if (result is NetworkResult.Success) {
            mainPreferences.setProductUpdated()
        } else if (result is NetworkResult.Error) {
            throw Exception(result.message)
        }
    }

    fun fetchProductImages() = fetchTable(homeRepository::fetchAndSaveProductImages, productImages)
    fun fetchProductPacking() =
        fetchTable(homeRepository::fetchAndSaveProductPacking, productPacking)

    fun fetchCustomers() = fetchTable(homeRepository::fetchAndSaveCustomers, customers)
    fun fetchCustomerDirections() =
        fetchTable(homeRepository::fetchAndSaveCustomerDirections, customerDirections)

    fun fetchAssignDirectionCustomer() =
        fetchTable(homeRepository::fetchAndSaveAssignDirectionCustomer, assignDirectionCustomer)

    fun fetchInvoiceCategory() =
        fetchTable(homeRepository::fetchAndSaveInvoiceCategory, invoiceCategory)

    fun fetchPattern() =
        fetchTable(homeRepository::fetchAndSavePattern, pattern)

    suspend fun syncPattern() {
        pattern.postValue(NetworkResult.Loading)

        val result = homeRepository.fetchAndSavePattern()
        pattern.postValue(result)

        if (result is NetworkResult.Success) {
            mainPreferences.setPatternUpdated()
        } else if (result is NetworkResult.Error) {
            throw Exception(result.message)
        }
    }

    fun fetchPatternDetails() =
        fetchTable(homeRepository::fetchAndSavePatternDetails, patternDetails)

    fun fetchAct() = fetchTable(homeRepository::fetchAndSaveAct, act)

    suspend fun syncAct() {
        act.postValue(NetworkResult.Loading)

        val result = homeRepository.fetchAndSaveAct()
        act.postValue(result)

        if (result is NetworkResult.Success) {
            mainPreferences.setActUpdated()
        } else if (result is NetworkResult.Error) {
            throw Exception(result.message)
        }
    }


    fun fetchVat() = fetchTable(homeRepository::fetchAndSaveVat, vat)
    fun fetchSaleCenter() = fetchTable(homeRepository::fetchAndSaveSaleCenter, saleCenter)
    fun fetchDiscount() = fetchTable(homeRepository::fetchAndSaveDiscount, discount)
    suspend fun syncDiscount() {
        discount.postValue(NetworkResult.Loading)

        val result = homeRepository.fetchAndSaveDiscount()
        discount.postValue(result)

        if (result is NetworkResult.Success) {
            mainPreferences.setDiscountUpdated()
        } else if (result is NetworkResult.Error) {
            throw Exception(result.message)
        }
    }

    // ------------------- تابع عمومی -------------------
    private fun <T> fetchTable(
        fetch: suspend () -> NetworkResult<List<T>>,
        liveData: MutableLiveData<NetworkResult<List<T>>>
    ) {
        viewModelScope.launch {
            liveData.value = NetworkResult.Loading
            liveData.value = fetch()
        }
    }

}


