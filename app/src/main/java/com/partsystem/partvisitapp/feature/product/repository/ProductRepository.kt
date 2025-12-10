package com.partsystem.partvisitapp.feature.product.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.partsystem.partvisitapp.core.database.dao.MojoodiDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.entity.MojoodiEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.mapper.toEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ProductFullData
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithRate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val dao: ProductDao,
    private val mojoodiDao: MojoodiDao,
    private val productImageDao: ProductImageDao,
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

    fun getProducts(groupProductId: Int?, actId: Int?): Flow<List<ProductWithPacking>> =
        dao.getProductsWithActDetails(groupProductId, actId)

    suspend fun getProductByActId(id: Int, actId: Int): ProductWithPacking? {
        return dao.getProductWithRate(id, actId)
    }



    suspend fun fetchAndSaveMojoodi(
        anbarId: Int,
        productId: Int,
        persianDate: String
    ): NetworkResult<List<MojoodiEntity>> {
        try {
            val response = api.getMojoodi(anbarId, productId, persianDate)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val mojoodiList = body.map { it.toEntity() }
            mojoodiDao.clearMojoodi()

            mojoodiDao.insertMojoodi(mojoodiList)

            return NetworkResult.Success(mojoodiList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // گرفتن موجودی از Room
    suspend fun getMojoodi(anbarId: Int, productId: Int): MojoodiEntity? {
        return mojoodiDao.getMojoodi(anbarId, productId)
    }
}
