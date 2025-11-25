package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ActWithDetails
import com.partsystem.partvisitapp.core.database.entity.VatWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ActDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActs(list: List<ActEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActDetails(list: List<ActDetailEntity>)

    @Query("SELECT * FROM act_table")
    fun getAllActs(): Flow<List<ActEntity>>

    @Transaction
    @Query("SELECT * FROM act_table")
    suspend fun getActWithDetails(): List<ActWithDetails>

    @Query("DELETE FROM act_table")
    suspend fun clearAct()

    @Query("DELETE FROM act_detail_table")
    suspend fun clearActDetails()
}
