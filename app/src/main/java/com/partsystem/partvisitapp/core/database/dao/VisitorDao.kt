package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.VisitorEntity

@Dao
interface VisitorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visitors: List<VisitorEntity>)

    @Query("SELECT * FROM Visitor")
    suspend fun getAll(): List<VisitorEntity>

    @Query("DELETE FROM Visitor")
    suspend fun clearVisitors()
}
