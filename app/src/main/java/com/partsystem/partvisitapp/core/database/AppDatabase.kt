package com.partsystem.partvisitapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.partsystem.partvisitapp.core.database.dao.*
import com.partsystem.partvisitapp.core.database.entity.*

@Database(
    entities = [ApplicationSettingEntity::class, VisitorEntity::class, VisitScheduleEntity::class,
        VisitScheduleDetailEntity::class, OrderEntity::class, GroupProductEntity::class,
        ProductEntity::class, ProductImageEntity::class, ProductPackingEntity::class,
        CustomerEntity::class, CustomerDirectionEntity::class, AssignDirectionCustomerEntity::class,
        InvoiceCategoryEntity::class, InvoiceCategoryDetailEntity::class,
        InvoiceCategoryCenterEntity::class,
        PatternEntity::class, PatternDetailEntity::class,
        ActEntity::class, ActDetailEntity::class,
        VatEntity::class, VatDetailEntity::class,
        SaleCenterEntity::class, SaleCenterAnbarEntity::class, SaleCenterUserEntity::class,
        DiscountEntity::class, FactorEntity::class],
    version = 1,
    exportSchema = false
)


abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationSettingDao(): ApplicationSettingDao
    abstract fun visitorDao(): VisitorDao
    abstract fun visitScheduleDao(): VisitScheduleDao
    abstract fun orderDao(): OrderDao
    abstract fun groupProductDao(): GroupProductDao
    abstract fun productDao(): ProductDao
    abstract fun productImageDao(): ProductImageDao
    abstract fun productPackingDao(): ProductPackingDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerDirectionDao(): CustomerDirectionDao
    abstract fun assignDirectionCustomerDao(): AssignDirectionCustomerDao
    abstract fun invoiceCategoryDao(): InvoiceCategoryDao
    abstract fun patternDao(): PatternDao
    abstract fun patternDetailDao(): PatternDetailDao
    abstract fun actDao(): ActDao
    abstract fun vatDao(): VatDao
    abstract fun saleCenterDao(): SaleCenterDao
    abstract fun discountDao(): DiscountDao
    abstract fun factorDao(): FactorDao
}

