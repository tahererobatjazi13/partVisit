package com.partsystem.partvisitapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.partsystem.partvisitapp.core.database.dao.*
import com.partsystem.partvisitapp.core.database.entity.*

@Database(
    entities = [ApplicationSettingEntity::class, OrderEntity::class, GroupProductEntity::class,
        ProductEntity::class, ProductImageEntity::class, ProductPackingEntity::class,
        CustomerEntity::class, CustomerDirectionEntity::class,
        InvoiceCategoryEntity::class, PatternEntity::class,
        ActEntity::class, ActDetailEntity::class],
    version = 1,
    exportSchema = false
)


abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationSettingDao(): ApplicationSettingDao
    abstract fun orderDao(): OrderDao
    abstract fun groupProductDao(): GroupProductDao
    abstract fun productDao(): ProductDao
    abstract fun productImageDao(): ProductImageDao
    abstract fun productPackingDao(): ProductPackingDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerDirectionDao(): CustomerDirectionDao
    abstract fun invoiceCategoryDao(): InvoiceCategoryDao
    abstract fun patternDao(): PatternDao
    abstract fun actDao(): ActDao
}

