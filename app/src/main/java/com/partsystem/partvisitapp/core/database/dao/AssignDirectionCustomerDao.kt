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

    @Query("SELECT * FROM AssignDirectionCustomer WHERE tafsiliId = :visitorId")
    suspend fun getByVisitor(visitorId: Int): List<AssignDirectionCustomerEntity>

    @Query("DELETE FROM AssignDirectionCustomer")
    suspend fun clearAssignDirectionCustomer()

    @Query("SELECT * FROM AssignDirectionCustomer WHERE CustomerId = :customerId")
    suspend fun getAssignDirectionCustomerByCustomerId(customerId: Int): List<AssignDirectionCustomerEntity>

}
