package com.partsystem.partvisitapp.feature.main.home.repository

import android.content.Context
import android.util.Log
import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.ApplicationSettingDao
import com.partsystem.partvisitapp.core.database.dao.AssignDirectionCustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDirectionDao
import com.partsystem.partvisitapp.core.database.dao.DiscountDao
import com.partsystem.partvisitapp.core.database.dao.GroupProductDao
import com.partsystem.partvisitapp.core.database.dao.InvoiceCategoryDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.dao.ProductPackingDao
import com.partsystem.partvisitapp.core.database.dao.SaleCenterDao
import com.partsystem.partvisitapp.core.database.dao.VatDao
import com.partsystem.partvisitapp.core.database.dao.VisitorDao
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity
import com.partsystem.partvisitapp.core.database.entity.AssignDirectionCustomerEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterAnbarEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterUserEntity
import com.partsystem.partvisitapp.core.database.entity.VatEntity
import com.partsystem.partvisitapp.core.database.entity.VisitorEntity
import com.partsystem.partvisitapp.core.database.mapper.toEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class HomeRepository @Inject constructor(
    private val api: ApiService,
    private val userPreferences: UserPreferences,
    private val applicationSettingDao: ApplicationSettingDao,
    private val visitorDao: VisitorDao,
    private val groupProductDao: GroupProductDao,
    private val productDao: ProductDao,
    private val productImageDao: ProductImageDao,
    private val productPackingDao: ProductPackingDao,
    private val customerDao: CustomerDao,
    private val customerDirectionDao: CustomerDirectionDao,
    private val assignDirectionCustomerDao: AssignDirectionCustomerDao,
    private val invoiceCategoryDao: InvoiceCategoryDao,
    private val patternDao: PatternDao,
    private val actDao: ActDao,
    private val vatDao: VatDao,
    private val saleCenterDao: SaleCenterDao,
    private val discountDao: DiscountDao,

    @ApplicationContext private val context: Context
) {
    suspend fun fetchAndSaveApplicationSetting(): NetworkResult<List<ApplicationSettingEntity>> {
        return try {
            val response = api.getApplicationSetting()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val applicationSettingList = body.map { it.toEntity() }

            applicationSettingDao.clearAll()
            applicationSettingDao.insertAll(applicationSettingList)

            NetworkResult.Success(applicationSettingList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveVisitor(): NetworkResult<List<VisitorEntity>> {
        return try {
            val response = api.getVisitors()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val visitorList = body.map { it.toEntity() }

            visitorDao.clearAll()
            visitorDao.insertAll(visitorList)

            NetworkResult.Success(visitorList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveGroups(): NetworkResult<List<GroupProductEntity>> {
        return try {
            val response = api.getGroupProducts()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val groupList = body.map { it.toEntity() }

            groupProductDao.clearAll()
            groupProductDao.insertAll(groupList)

            NetworkResult.Success(groupList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveProducts(): NetworkResult<List<ProductEntity>> {
        return try {
            val response = api.getProducts()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val productList = body.map { it.toEntity() }

            productDao.clearAll()
            productDao.insertProducts(productList)

            NetworkResult.Success(productList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveImages(): NetworkResult<List<ProductImageEntity>> {
        return try {
            val response = api.getProductImages()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server error: ${response.code()}")
            }

            val imageList = body.map { it.toEntity(context) }

            productImageDao.clearAll()
            productImageDao.insertImages(imageList)

            NetworkResult.Success(imageList)

        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveProductPacking(): NetworkResult<List<ProductPackingEntity>> {
        return try {
            val response = api.getProductPacking()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val productPackingList = body.map { it.toEntity() }

            productPackingDao.clearAll()
            productPackingDao.insertAll(productPackingList)

            NetworkResult.Success(productPackingList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveCustomers(): NetworkResult<List<CustomerEntity>> {
        return try {
            val visitorId = userPreferences.personnelId.first() ?: 0
            val response = api.getCustomers(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val customerList = body.map { it.toEntity() }

            customerDao.clearAll()
            customerDao.insertCustomers(customerList)

            NetworkResult.Success(customerList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveCustomerDirections(): NetworkResult<List<CustomerDirectionEntity>> {
        return try {
            val visitorId = userPreferences.personnelId.first() ?: 0
            val response = api.getCustomerDirections(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val customerDirectionList = body.map { it.toEntity() }

            customerDirectionDao.clearAll()
            customerDirectionDao.insertAll(customerDirectionList)

            NetworkResult.Success(customerDirectionList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveAssignDirectionCustomer(): NetworkResult<List<AssignDirectionCustomerEntity>> {
        return try {
            val visitorId = userPreferences.personnelId.first() ?: 0
            val response = api.getAssignDirectionCustomer(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val assignDirectionCustomerList = body.map { it.toEntity() }

            assignDirectionCustomerDao.clearAll()
            assignDirectionCustomerDao.insertAll(assignDirectionCustomerList)

            NetworkResult.Success(assignDirectionCustomerList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveInvoiceCategory(): NetworkResult<List<InvoiceCategoryEntity>> {
        return try {
            val response = api.getInvoiceCategories()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val invoiceCategoryList = body.map { it.toEntity() }

            invoiceCategoryDao.clearAll()
            invoiceCategoryDao.insertAll(invoiceCategoryList)

            NetworkResult.Success(invoiceCategoryList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSavePattern(): NetworkResult<List<PatternEntity>> {
        return try {
            val visitorId = userPreferences.personnelId.first() ?: 0
            val response = api.getPattern(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val patternList = body.map { it.toEntity() }

            patternDao.clearAll()
            patternDao.insertAll(patternList)

            NetworkResult.Success(patternList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveAct(): NetworkResult<List<ActEntity>> {
         try {
            val response = api.getAct()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val actList = body.map { it.toEntity() }

            val actDetailList = body.flatMap { act ->
                act.actDetails?.map { it.toEntity() } ?: emptyList()
            }

            actDao.clearAct()
            actDao.clearActDetails()

            actDao.insertActs(actList)
            actDao.insertActDetails(actDetailList)

            return NetworkResult.Success(actList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveVat(): NetworkResult<List<VatEntity>> {
         try {
            val response = api.getVat()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val vatList = body.map { it.toEntity() }

            val vatDetailList = body.flatMap { act ->
                act.vatDetails.map { it.toEntity() }
            }

            vatDao.clearVat()
            vatDao.clearVatDetails()

            vatDao.insertVat(vatList)
            vatDao.insertVatDetails(vatDetailList)

            return NetworkResult.Success(vatList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveSaleCenter(): NetworkResult<List<SaleCenterEntity>> {
         try {
            val response = api.getSaleCenters()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val saleCenterList = body.map { it.toEntity() }

            val saleCenterAnbarsList = body.flatMap { saleCenter ->
                saleCenter.saleCenterAnbars.map {
                    SaleCenterAnbarEntity(
                        saleCenterId = it.saleCenterId,
                        anbarId = it.anbarId,
                        isActive = it.isActive
                    )
                }
            }

            val saleCenterUsersList = body.flatMap { saleCenter ->
                saleCenter.saleCenterUsers.map {
                    SaleCenterUserEntity(
                        saleCenterId = it.saleCenterId,
                        userId = it.userId
                    )
                }
            }

            saleCenterDao.clearCenters()
            saleCenterDao.clearAnbars()
            saleCenterDao.clearUsers()

            saleCenterList.forEach { saleCenterDao.insertSaleCenter(it) }
            saleCenterDao.insertAnbars(saleCenterAnbarsList)
            saleCenterDao.insertUsers(saleCenterUsersList)

            return NetworkResult.Success(saleCenterList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchAndSaveDiscount(): NetworkResult<List<DiscountEntity>> {
        try {
            val response = api.getDiscounts()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val discountList = body.map { it.toEntity() }

            discountDao.clearDiscounts()

            discountDao.insertDiscounts(discountList)

            return NetworkResult.Success(discountList)

        } catch (e: Exception) {
            Log.e("NetworkError", e.toString())
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

}


