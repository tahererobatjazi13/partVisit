package com.partsystem.partvisitapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.partsystem.partvisitapp.core.database.dao.*
import com.partsystem.partvisitapp.core.database.entity.*

@Database(
    entities = [OrderEntity::class, GroupProductEntity::class, ProductEntity::class, CustomerEntity::class, CustomerDirectionEntity::class,
        ProductImageEntity::class, InvoiceCategoryEntity::class, PatternEntity::class,
        ActEntity::class,ActDetailEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun groupProductDao(): GroupProductDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerDirectionDao(): CustomerDirectionDao
    abstract fun productImageDao(): ProductImageDao
    abstract fun invoiceCategoryDao(): InvoiceCategoryDao
    abstract fun patternDao(): PatternDao
    abstract fun actDao(): ActDao
}

