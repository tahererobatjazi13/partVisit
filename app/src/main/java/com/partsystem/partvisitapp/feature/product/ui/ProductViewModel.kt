package com.partsystem.partvisitapp.feature.product.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.utils.ImageProductType
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

    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages



/*    // برای نگه داشتن عکس‌ها
    private val _productImages = MutableLiveData<Map<Int, List<ProductImageEntity>>>()
    val productImages: LiveData<Map<Int, List<ProductImageEntity>>> = _productImages*/
    init {
        viewModelScope.launch {
            repository.getAllProducts().collectLatest { list ->
                _productList.value = list
                _filteredList.value = list
            }
        }

    repository.getAllProductImages().observeForever { allImages ->

        val groupImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.GROUP_PRODUCT }
        val productImages = allImages.filter { it.value.firstOrNull()?.ownerType == ImageProductType.PRODUCT }

        _groupProductImages.postValue(groupImages)
        _productImages.postValue(productImages)
    }

/*
    // لود کردن همه عکس‌ها یک بار
        repository.getAllProductImages().observeForever {
            _productImages.postValue(it)
        }*/
    }
}
