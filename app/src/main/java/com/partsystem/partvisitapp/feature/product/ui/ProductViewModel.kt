package com.partsystem.partvisitapp.feature.product.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.dao.ProductWithPacking
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductModel
import com.partsystem.partvisitapp.core.utils.ImageProductType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

/*
    // لیست اصلی محصولات از Room
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


    private val _groupProductImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val groupProductImages: LiveData<Map<Int, List<ProductImageEntity>>> = _groupProductImages

    // تصاویر محصولات
    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages



*//*    // برای نگه داشتن عکس‌ها
    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages*//*
    init {
        viewModelScope.launch {
            repository.getAllProducts().collectLatest { list ->
                _productList.value = list
                _filteredList.value = list
            }
        }

    // گرفتن همه تصاویر محصولات
    repository.getAllProductImages().observeForever { allImages ->

        val groupImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.GROUP_PRODUCT }
        val productImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.PRODUCT }

        _groupProductImages.postValue(groupImages)
        _productImages.postValue(productImages)
    }
    }

    // محصولات فاکتور یا کوئری شده
    private val _products = MutableLiveData<List<ProductModel>>()
    val products: LiveData<List<ProductModel>> = _products

    // گرفتن محصولات همراه با rate و packings برای Adapter
    fun loadProducts(groupId: Int?, actId: Int?) {
        viewModelScope.launch {
            val list = repository.getProducts(groupId, actId)
            _products.value = list
        }
    }*/
private val _productList = MutableLiveData<List<ProductWithPacking>>()
    val productList: LiveData<List<ProductWithPacking>> = _productList

    private val _filteredList = MutableLiveData<List<ProductWithPacking>>()
    val filteredList: LiveData<List<ProductWithPacking>> = _filteredList

    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages

    fun loadProducts(groupProductId: Int? = null, actId: Int? = null) {
        viewModelScope.launch {
            repository.getProducts(groupProductId, actId).collect { list ->
                _productList.value = list
                _filteredList.value = list
            }
        }

        repository.getAllProductImages().observeForever { images ->
            _productImages.value = images
        }
    }

    fun filterProducts(query: String) {
        val list = _productList.value ?: emptyList()
        _filteredList.value = if (query.isEmpty()) {
            list
        } else {
            list.filter { it.product.name?.contains(query, ignoreCase = true) == true }
        }
    }
    // گرفتن محصول با id مشخص
    fun getProductById(id: Int): LiveData<ProductEntity> = repository.getProductById(id)

}
