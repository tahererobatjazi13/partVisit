package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity

@Dao
interface DiscountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscounts(discounts: List<DiscountEntity>)

    @Query("SELECT * FROM discount_table")
    suspend fun getAllDiscounts(): List<DiscountEntity>

    @Query("DELETE FROM discount_table")
    suspend fun clearDiscounts()
}
