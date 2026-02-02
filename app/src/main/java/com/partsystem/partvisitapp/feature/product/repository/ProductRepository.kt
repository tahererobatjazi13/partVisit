package com.partsystem.partvisitapp.feature.product.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.partsystem.partvisitapp.core.database.dao.ApplicationSettingDao
import com.partsystem.partvisitapp.core.database.dao.MojoodiDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.MojoodiEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.mapper.toEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import com.partsystem.partvisitapp.feature.create_order.model.MojoodiDto
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProductRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val dao: ProductDao,
    private val mojoodiDao: MojoodiDao,
    private val applicationSettingDao: ApplicationSettingDao,
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

    fun getProductByActId(id: Int, actId: Int): ProductWithPacking? {
        return dao.getProductWithRate(id, actId)
    }

    fun getProductByActId2(id: Int, actId: Int): Flow<ProductWithPacking> =
        dao.getProductWithRate2(id, actId)


    // گرفتن موجودی از Room
    suspend fun getMojoodi(anbarId: Int, productId: Int): MojoodiEntity? {
        return mojoodiDao.getMojoodi(anbarId, productId)
    }

    suspend fun getDistributionMojoodiSetting(): Int {
        return applicationSettingDao.getSettingByName("DistributionMojoodi")
            ?.value?.toIntOrNull() ?: 1 // Default: NoAction
    }

    // Check  Mojoodi
        suspend fun checkMojoodi(
            anbarId: Int,
            productId: Int,
            persianDate: String
        ): NetworkResult<List<MojoodiDto>> {

            return try {
                val response = api.checkMojoodi(anbarId, productId, persianDate)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    NetworkResult.Success(body)
                } else {
                    val errorMsg = ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                    NetworkResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                NetworkResult.Error(getExceptionMessage(context, e))
            }
        }

}
