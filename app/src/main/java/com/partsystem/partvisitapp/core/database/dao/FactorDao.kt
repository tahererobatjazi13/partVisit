package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(item: FactorDetailEntity)

    @Query("SELECT * FROM factor_detail_table")
    fun getAllFactorDetail(): LiveData<List<FactorDetailEntity>>

    @Query("DELETE FROM factor_detail_table WHERE productId = :productId")
    suspend fun deleteFactorDetail(productId: Int)
}
