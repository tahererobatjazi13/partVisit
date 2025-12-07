package com.partsystem.partvisitapp.feature.product.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.dao.ProductWithPacking
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductModel
import com.partsystem.partvisitapp.core.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ProductRepository @Inject constructor(
    private val dao: ProductDao,
    private val productImageDao: ProductImageDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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


/*

    // گرفتن محصولات همراه با rate و بسته‌بندی‌ها
    suspend fun getProducts(groupId: Int?, actId: Int?): List<ProductModel> = withContext(ioDispatcher) {
        if (actId == null) return@withContext emptyList()

        val productList = dao.getProductsWithRate(actId, groupId)

        productList.map { p ->
            val packings = dao.getPacking(p.productId)
            ProductModel(
                id = p.productId,
                code = p.code,
                name = p.name,
                description = p.description,
                rate = p.rate,
                toll = p.toll,
                vat = p.vat,
                rateAfterVatAndToll = p.rateAfterVatAndToll,
                fileName = p.fileName,
                packings = packings
            )
        }
    }
*/

    fun getProducts(groupProductId: Int?, actId: Int?): Flow<List<ProductWithPacking>> =
        dao.getProductsWithActDetails(groupProductId, actId)

}
