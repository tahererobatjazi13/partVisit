package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.PatternDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

@Dao
interface PatternDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PatternDetailEntity>)

    @Query("SELECT * FROM PatternDetail")
    suspend fun getAll(): List<PatternDetailEntity>

    @Query("DELETE FROM PatternDetail")
    suspend fun clearPatternDetails()
}
