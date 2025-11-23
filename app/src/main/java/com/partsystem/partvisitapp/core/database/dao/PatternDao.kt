package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PatternEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PatternEntity)

    @Query("SELECT * FROM pattern_table")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM pattern_table WHERE id = :id")
    suspend fun getById(id: Int): PatternEntity?

    @Query("DELETE FROM pattern_table")
    suspend fun clearAll()
}
