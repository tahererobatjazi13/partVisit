package com.partsystem.partvisitapp.core.network


import com.partsystem.partvisitapp.feature.main.home.model.GroupProductDto
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.main.home.model.ActDto
import com.partsystem.partvisitapp.feature.main.home.model.ApplicationSettingDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDirectionDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.InvoiceCategoryDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductImageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {

    @POST("LoginUser")
    suspend fun loginUser(
        @Query("UserName") userName: String,
        @Query("Password") password: String
    ): Response<LoginResponse>

    @GET("ApplicationSetting")
    suspend fun getApplicationSetting(): Response<List<ApplicationSettingDto>>

    @GET("GroupProduct")
    suspend fun getGroupProducts(): Response<List<GroupProductDto>>

    @GET("Product")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("ProductImage")
    suspend fun getProductImages(): Response<List<ProductImageDto>>

    @GET("Customer")
    suspend fun getCustomers(@Query("visitorId") visitorId: Int): Response<List<CustomerDto>>

    @GET("CustomerDirection")
    suspend fun getCustomerDirections(@Query("visitorId") visitorId: Int): Response<List<CustomerDirectionDto>>

    @GET("InvoiceCategory")
    suspend fun getInvoiceCategories(): Response<List<InvoiceCategoryDto>>

    @GET("Pattern")
    suspend fun getPattern(@Query("visitorId") visitorId: Int): Response<List<PatternDto>>

    @GET("Act")
    suspend fun getAct(): Response<List<ActDto>>

}
