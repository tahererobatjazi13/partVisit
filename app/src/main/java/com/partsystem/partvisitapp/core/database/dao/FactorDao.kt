package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity

@Dao
interface FactorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFactor(factor: FactorHeaderEntity): Long

    @Update
    fun updateFactor(factor: FactorHeaderEntity)

    @Delete
    fun deleteFactor(factor: FactorHeaderEntity)

    @Query("SELECT * FROM factor_header_table WHERE id = :id")
    fun getFactorById(id: Int): FactorHeaderEntity?

    @Query("SELECT * FROM factor_header_table")
    suspend fun getAllFactors(): List<FactorHeaderEntity>
}
