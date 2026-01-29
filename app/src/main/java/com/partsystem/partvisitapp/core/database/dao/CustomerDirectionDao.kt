package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDirectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<CustomerDirectionEntity>)


    @Query("SELECT * FROM CustomerDirection WHERE customerId = :customerId")
     fun getCustomerDirectionsByCustomer(customerId: Int): Flow<List<CustomerDirectionEntity>>

    @Query("DELETE FROM CustomerDirection WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: Int)

    @Query("DELETE FROM CustomerDirection")
    suspend fun clearCustomerDirection()


}
