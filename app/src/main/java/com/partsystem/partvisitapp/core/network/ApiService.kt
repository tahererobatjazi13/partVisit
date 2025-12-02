package com.partsystem.partvisitapp.core.network


import com.partsystem.partvisitapp.core.network.modelDto.GroupProductDto
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.core.network.modelDto.ActDto
import com.partsystem.partvisitapp.core.network.modelDto.ApplicationSettingDto
import com.partsystem.partvisitapp.core.network.modelDto.AssignDirectionCustomerDto
import com.partsystem.partvisitapp.core.network.modelDto.CustomerDirectionDto
import com.partsystem.partvisitapp.core.network.modelDto.CustomerDto
import com.partsystem.partvisitapp.core.network.modelDto.DiscountDto
import com.partsystem.partvisitapp.core.network.modelDto.InvoiceCategoryDto
import com.partsystem.partvisitapp.core.network.modelDto.PatternDetailDto
import com.partsystem.partvisitapp.core.network.modelDto.PatternDto
import com.partsystem.partvisitapp.core.network.modelDto.ProductDto
import com.partsystem.partvisitapp.core.network.modelDto.ProductImageDto
import com.partsystem.partvisitapp.core.network.modelDto.ProductPackingDto
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.network.modelDto.SaleCenterDto
import com.partsystem.partvisitapp.core.network.modelDto.VatDto
import com.partsystem.partvisitapp.core.network.modelDto.VisitScheduleDto
import com.partsystem.partvisitapp.core.network.modelDto.VisitorDto
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

    @GET("Visitor")
    suspend fun getVisitors(@Query("visitorId") visitorId: Int): Response<List<VisitorDto>>

    @GET("VisitSchedule")
    suspend fun getVisitSchedule(@Query("visitorId") visitorId: Int): Response<List<VisitScheduleDto>>

    @GET("GroupProduct")
    suspend fun getGroupProducts(): Response<List<GroupProductDto>>

    @GET("Product")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("ProductImage")
    suspend fun getProductImages(): Response<List<ProductImageDto>>

    @GET("ProductPacking")
    suspend fun getProductPacking(): Response<List<ProductPackingDto>>

    @GET("Customer")
    suspend fun getCustomers(@Query("visitorId") visitorId: Int): Response<List<CustomerDto>>

    @GET("CustomerDirection")
    suspend fun getCustomerDirections(@Query("visitorId") visitorId: Int): Response<List<CustomerDirectionDto>>

    @GET("AssignDirectionCustomer")
    suspend fun getAssignDirectionCustomer(@Query("visitorId") visitorId: Int): Response<List<AssignDirectionCustomerDto>>

    @GET("InvoiceCategory")
    suspend fun getInvoiceCategories(): Response<List<InvoiceCategoryDto>>

    @GET("Pattern")
    suspend fun getPattern(@Query("visitorId") visitorId: Int): Response<List<PatternDto>>

    @GET("PatternDetail")
    suspend fun getPatternDetails(@Query("visitorId") visitorId: Int): Response<List<PatternDetailDto>>
    @GET("Act")
    suspend fun getAct(): Response<List<ActDto>>

    @GET("Vat")
    suspend fun getVat(): Response<List<VatDto>>

    @GET("SaleCenter")
    suspend fun getSaleCenters(): Response<List<SaleCenterDto>>

    @GET("Discount")
    suspend fun getDiscounts(): Response<List<DiscountDto>>


    @GET("ReportFactor")
    suspend fun getReportFactorVisitor(
        @Query("type") type: Int,
        @Query("VisitorId") visitorId: Int
    ): Response<List<ReportFactorDto>>

    @GET("ReportFactor")
    suspend fun getReportFactorDetail(
        @Query("type") type: Int,
        @Query("FactorId") factorId: Int
    ): Response<List<ReportFactorDto>>

    @GET("ReportFactor")
    suspend fun getReportFactorCustomer(
        @Query("type") type: Int,
        @Query("CustomerId") customerId: Int
    ): Response<List<ReportFactorDto>>

}
