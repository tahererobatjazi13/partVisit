package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDirectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<CustomerDirectionEntity>)


    @Query("SELECT * FROM customer_direction_table WHERE customerId = :customerId")
     fun getCustomerDirectionsByCustomer(customerId: Int): Flow<List<CustomerDirectionEntity>>

    @Query("DELETE FROM customer_direction_table WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: Int)

    @Query("DELETE FROM customer_direction_table")
    suspend fun clearCustomerDirection()


}
