package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.AssignDirectionCustomerEntity

@Dao
interface AssignDirectionCustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AssignDirectionCustomerEntity>)

    @Query("SELECT * FROM assign_direction_customer_table WHERE tafsiliId = :visitorId")
    suspend fun getByVisitor(visitorId: Int): List<AssignDirectionCustomerEntity>

    @Query("DELETE FROM assign_direction_customer_table")
    suspend fun clearAll()
}
