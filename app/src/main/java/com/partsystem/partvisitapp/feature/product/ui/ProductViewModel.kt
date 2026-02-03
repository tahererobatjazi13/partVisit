package com.partsystem.partvisitapp.feature.product.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.MojoodiEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.ImageProductType
import com.partsystem.partvisitapp.feature.create_order.model.MojoodiDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    // لیست اصلی محصولات از دیتابیس
    private val _productList = MutableLiveData<List<ProductEntity>>()

    // لیست فیلترشده
    private val _filteredList = MutableLiveData<List<ProductEntity>>()
    val filteredList: LiveData<List<ProductEntity>> get() = _filteredList

    init {
        // جمع‌آوری لیست محصولات از Room و مقداردهی اولیه
        viewModelScope.launch {
            repository.getAllProducts().collectLatest { list ->
                _productList.value = list
                _filteredList.value = list
            }
        }
    }

    // فیلتر کردن محصولات بر اساس query
    fun filterProducts(query: String) {
        val list = _productList.value ?: emptyList()
        _filteredList.value = if (query.isEmpty()) {
            list
        } else {
            list.filter { it.name?.contains(query, ignoreCase = true) == true }
        }
    }

    // گرفتن محصول با id مشخص
    fun getProductById(id: Int): LiveData<ProductEntity> = repository.getProductById(id)

    private val _groupProductImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val groupProductImages: LiveData<Map<Int, List<ProductImageEntity>>> = _groupProductImages

    // برای نگه داشتن عکس‌ها
    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages

    init {
        viewModelScope.launch {
            repository.getAllProducts().collectLatest { list ->
                _productList.value = list
                _filteredList.value = list
            }
        }

        // لود کردن همه عکس‌ها یک بار
        repository.getAllProductImages().observeForever { allImages ->

            val groupImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.GROUP_PRODUCT }
            val productImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.PRODUCT }

            _groupProductImages.postValue(groupImages)
            _productImages.postValue(productImages)
        }
    }

    private val _productWithActList = MutableLiveData<List<ProductWithPacking>>()

    private val _filteredWithActList = MutableLiveData<List<ProductWithPacking>>()
    val filteredWithActList: LiveData<List<ProductWithPacking>> = _filteredWithActList

    fun loadProductsWithAct(groupProductId: Int? = null, actId: Int? = null) {
        viewModelScope.launch {
            repository.getProducts(groupProductId, actId).collect { list ->
                _productWithActList.value = list
                _filteredWithActList.value = list
            }
        }
        repository.getAllProductImages().observeForever { images ->
            _productImages.value = images
        }
    }

    fun filterProductsWithAct(query: String) {
        val list = _productWithActList.value ?: emptyList()
        _filteredWithActList.value = if (query.isEmpty()) {
            list
        } else {
            list.filter { it.product.name?.contains(query, ignoreCase = true) == true }
        }
    }

 /*   private val _mojoodi = MutableStateFlow<MojoodiEntity?>(null)
    val mojoodi: StateFlow<MojoodiEntity?> get() = _mojoodi

    fun loadMojoodi(anbarId: Int, productId: Int, persianDate: String) {
        viewModelScope.launch {
            repository.fetchAndSaveMojoodi(anbarId, productId, persianDate)
            val data = repository.getMojoodi(anbarId, productId)
            _mojoodi.value = data
        }
    }*/

    private val _checkMojoodi =
        MutableLiveData<NetworkResult<List<MojoodiDto>>?>()

    val checkMojoodi: LiveData<NetworkResult<List<MojoodiDto>>?> =
        _checkMojoodi

    fun checkMojoodi(
        anbarId: Int,
        productId: Int,
        persianDate: String
    ) {
        viewModelScope.launch {
            _checkMojoodi.value = NetworkResult.Loading
            _checkMojoodi.value =
                repository.checkMojoodi(anbarId, productId, persianDate)
        }
    }

    fun clearCheckMojoodi() {
        _checkMojoodi.value = null
    }

}

