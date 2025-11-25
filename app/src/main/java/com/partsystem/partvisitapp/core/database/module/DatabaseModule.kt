package com.partsystem.partvisitapp.core.database.module

import android.content.Context
import androidx.room.Room
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "part_db")
            .build()

    @Provides
    fun provideApplicationSettingDao(db: AppDatabase): ApplicationSettingDao =
        db.applicationSettingDao()

    @Provides
    fun provideVisitorDao(db: AppDatabase): VisitorDao =
        db.visitorDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao =
        db.orderDao()

    @Provides
    fun provideGroupProductDao(db: AppDatabase): GroupProductDao =
        db.groupProductDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao =
        db.productDao()

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao =
        db.customerDao()

    @Provides
    fun provideCustomerDirectionDao(db: AppDatabase): CustomerDirectionDao =
        db.customerDirectionDao()

    @Provides
    fun provideAssignDirectionCustomerDao(db: AppDatabase): AssignDirectionCustomerDao =
        db.assignDirectionCustomerDao()


    @Provides
    fun provideProductImageDao(db: AppDatabase): ProductImageDao =
        db.productImageDao()

    @Provides
    fun provideProductPackingDao(db: AppDatabase): ProductPackingDao =
        db.productPackingDao()

    @Provides
    fun provideInvoiceCategoryDao(db: AppDatabase): InvoiceCategoryDao =
        db.invoiceCategoryDao()

    @Provides
    fun providePatternDao(db: AppDatabase): PatternDao =
        db.patternDao()

    @Provides
    fun provideActDao(db: AppDatabase): ActDao =
        db.actDao()

    @Provides
    fun provideVatDao(db: AppDatabase): VatDao =
        db.vatDao()

    @Provides
    fun provideSaleCenterDao(db: AppDatabase): SaleCenterDao =
        db.saleCenterDao()

    @Provides
    fun provideDiscountDao(db: AppDatabase): DiscountDao =
        db.discountDao()
}
