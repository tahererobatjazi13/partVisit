package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.FactorEntity

@Dao
interface FactorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFactor(factor: FactorEntity): Long

    @Update
    fun updateFactor(factor: FactorEntity)

    @Delete
    fun deleteFactor(factor: FactorEntity)

    @Query("SELECT * FROM factor_table WHERE id = :id")
    fun getFactorById(id: Int): FactorEntity?

    @Query("SELECT * FROM factor_table")
    suspend fun getAllFactors(): List<FactorEntity>
}
