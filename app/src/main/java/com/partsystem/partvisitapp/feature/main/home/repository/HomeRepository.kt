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
import com.partsystem.partvisitapp.core.database.dao.MojoodiDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.PatternDetailDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductImageDao
import com.partsystem.partvisitapp.core.database.dao.ProductPackingDao
import com.partsystem.partvisitapp.core.database.dao.SaleCenterDao
import com.partsystem.partvisitapp.core.database.dao.VatDao
import com.partsystem.partvisitapp.core.database.dao.VisitScheduleDao
import com.partsystem.partvisitapp.core.database.dao.VisitorDao
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
import com.partsystem.partvisitapp.core.database.entity.SaleCenterAnbarEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterUserEntity
import com.partsystem.partvisitapp.core.database.entity.VatEntity
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleEntity
import com.partsystem.partvisitapp.core.database.entity.VisitorEntity
import com.partsystem.partvisitapp.core.database.mapper.toEntity
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class HomeRepository @Inject constructor(
    private val api: ApiService,
    private val mainPreferences: MainPreferences,
    private val applicationSettingDao: ApplicationSettingDao,
    private val visitorDao: VisitorDao,
    private val visitScheduleDao: VisitScheduleDao,
    private val groupProductDao: GroupProductDao,
    private val productDao: ProductDao,
    private val productImageDao: ProductImageDao,
    private val productPackingDao: ProductPackingDao,
    private val customerDao: CustomerDao,
    private val customerDirectionDao: CustomerDirectionDao,
    private val assignDirectionCustomerDao: AssignDirectionCustomerDao,
    private val invoiceCategoryDao: InvoiceCategoryDao,
    private val patternDao: PatternDao,
    private val patternDetailDao: PatternDetailDao,
    private val actDao: ActDao,
    private val vatDao: VatDao,
    private val saleCenterDao: SaleCenterDao,
    private val discountDao: DiscountDao,
    private val mojoodiDao: MojoodiDao,

    @ApplicationContext private val context: Context
) {

    private val visitorId: Int by lazy {
        runBlocking {
            mainPreferences.personnelId.first() ?: 0
        }
    }

    suspend fun fetchAndSaveApplicationSetting(): NetworkResult<List<ApplicationSettingEntity>> {
        return try {
            val response = api.getApplicationSetting()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val applicationSettingList = body.map { it.toEntity() }
            applicationSettingDao.insertAll(applicationSettingList)

            NetworkResult.Success(applicationSettingList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
        }
    }

    suspend fun getControlVisitSchedule(): Boolean {
        val setting = applicationSettingDao.getSettingByName("ControlVisitSchedule")
        return setting?.value?.equals("true", ignoreCase = true) ?: false
    }

    suspend fun fetchAndSaveVisitors(): NetworkResult<List<VisitorEntity>> {
        return try {

            val response = api.getVisitors(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val visitorList = body.map { it.toEntity() }
            visitorDao.insertAll(visitorList)

            NetworkResult.Success(visitorList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSaveVisitSchedules(): NetworkResult<List<VisitScheduleEntity>> {
        try {
            val response = api.getVisitSchedule(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }
            val visitScheduleList = body.map { it.toEntity() }

            val visitScheduleDetailList = body.flatMap { act ->
                act.visitScheduleDetails?.map { it.toEntity() } ?: emptyList()
            }

            visitScheduleDao.insertVisitSchedule(visitScheduleList)
            visitScheduleDao.insertVisitScheduleDetails(visitScheduleDetailList)

            return NetworkResult.Success(visitScheduleList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
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
            groupProductDao.insertAll(groupList)

            NetworkResult.Success(groupList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
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
            productDao.insertProducts(productList)

            NetworkResult.Success(productList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSaveProductImages(): NetworkResult<List<ProductImageEntity>> {
        return try {
            val response = api.getProductImages()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server error: ${response.code()}")
            }

            val imageList = body.map { it.toEntity(context) }
            productImageDao.insertImages(imageList)

            NetworkResult.Success(imageList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
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
            productPackingDao.insertAll(productPackingList)

            NetworkResult.Success(productPackingList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSaveCustomers(): NetworkResult<List<CustomerEntity>> {
        return try {
            val response = api.getCustomers(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val customerList = body.map { it.toEntity() }
            customerDao.insertCustomers(customerList)

            NetworkResult.Success(customerList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSaveCustomerDirections(): NetworkResult<List<CustomerDirectionEntity>> {
        return try {
            val response = api.getCustomerDirections(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val customerDirectionList = body.map { it.toEntity() }
            customerDirectionDao.insertAll(customerDirectionList)

            NetworkResult.Success(customerDirectionList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSaveAssignDirectionCustomer(): NetworkResult<List<AssignDirectionCustomerEntity>> {
        return try {
            val response = api.getAssignDirectionCustomer(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val assignDirectionCustomerList = body.map { it.toEntity() }
            assignDirectionCustomerDao.insertAll(assignDirectionCustomerList)

            NetworkResult.Success(assignDirectionCustomerList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
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
            invoiceCategoryDao.insertAll(invoiceCategoryList)

            NetworkResult.Success(invoiceCategoryList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSavePattern(): NetworkResult<List<PatternEntity>> {
        return try {
            val response = api.getPattern(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val patternList = body.map { it.toEntity() }
            patternDao.insertAll(patternList)

            NetworkResult.Success(patternList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun fetchAndSavePatternDetails(): NetworkResult<List<PatternDetailEntity>> {
        return try {
            val response = api.getPatternDetails(visitorId)
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return NetworkResult.Error("Server Error: ${response.code()}")
            }

            val patternDetailsList = body.map { it.toEntity() }
            patternDetailDao.insertAll(patternDetailsList)

            NetworkResult.Success(patternDetailsList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
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

            actDao.insertActs(actList)
            actDao.insertActDetails(actDetailList)

            return NetworkResult.Success(actList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
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

            vatDao.insertVat(vatList)
            vatDao.insertVatDetails(vatDetailList)

            return NetworkResult.Success(vatList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
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

            saleCenterList.forEach { saleCenterDao.insertSaleCenter(it) }
            saleCenterDao.insertAnbars(saleCenterAnbarsList)
            saleCenterDao.insertUsers(saleCenterUsersList)

            return NetworkResult.Success(saleCenterList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
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

            val discountEshantyunsList = body.flatMap { discount ->
                discount.discountEshantyuns?.map { it.toEntity() } ?: emptyList()
            }
            val discountGiftsList = body.flatMap { discount ->
                discount.discountGifts?.map { it.toEntity() } ?: emptyList()
            }
            val discountGroupsList = body.flatMap { discount ->
                discount.discountGroups?.map { it.toEntity() } ?: emptyList()
            }

            val discountProductKindInclusionsList = body.flatMap { discount ->
                discount.discountProductKindInclusions?.map { it.toEntity() } ?: emptyList()
            }
            val discountProductKindsList = body.flatMap { discount ->
                discount.discountProductKinds?.map { it.toEntity() } ?: emptyList()
            }
            val discountProductsList = body.flatMap { discount ->
                discount.discountProducts?.map { it.toEntity() } ?: emptyList()
            }

            val discountStairsList = body.flatMap { discount ->
                discount.discountStairs?.map { it.toEntity() } ?: emptyList()
            }
            val discountUsersList = body.flatMap { discount ->
                discount.discountUsers?.map { it.toEntity() } ?: emptyList()
            }
            val discountCustomersList = body.flatMap { discount ->
                discount.discountCustomers?.map { it.toEntity() } ?: emptyList()
            }

            discountDao.insertDiscounts(discountList)
            discountDao.insertDiscountEshantyuns(discountEshantyunsList)
            discountDao.insertDiscountGifts(discountGiftsList)
            discountDao.insertDiscountGroups(discountGroupsList)
            discountDao.insertDiscountProductKindInclusions(discountProductKindInclusionsList)
            discountDao.insertDiscountProductKinds(discountProductKindsList)
            discountDao.insertDiscountProducts(discountProductsList)
            discountDao.insertDiscountStairs(discountStairsList)
            discountDao.insertDiscountUsers(discountUsersList)
            discountDao.insertDiscountCustomers(discountCustomersList)

            return NetworkResult.Success(discountList)

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            return NetworkResult.Error(errorMsg)
        }
    }


}


