package com.partsystem.partvisitapp.feature.product.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProductRepository @Inject constructor(
    private val dao: ProductDao,
    private val productImageDao: ProductImageDao
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAllProducts()

    fun getProductById(id: Int): LiveData<ProductEntity> = dao.getProductById(id)

    suspend fun clearAll() = dao.clearProducts()

    fun getImagesForProduct(productId: Int): LiveData<List<ProductImageEntity>> =
        productImageDao.getImagesByProductId(productId)

    // LiveData با استفاده از liveData builder
    fun getAllProductImages(): LiveData<Map<Int, List<ProductImageEntity>>> = liveData {
        val allImages = productImageDao.getAllImagesOnce()
        val grouped = allImages.groupBy { it.ownerId }
        emit(grouped)
    }

}
