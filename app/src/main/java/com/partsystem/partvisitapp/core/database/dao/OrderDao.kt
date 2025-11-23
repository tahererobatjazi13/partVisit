package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.partsystem.partvisitapp.core.database.entity.OrderEntity

@Dao
interface OrderDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(item: OrderEntity)

        @Update
        suspend fun update(item: OrderEntity)

        @Delete
        suspend fun delete(item: OrderEntity)

        @Query("DELETE FROM order_table")
        suspend fun deleteAll()

        @Query("SELECT * FROM order_table WHERE quantity > 0")
        fun getAll(): LiveData<List<OrderEntity>>

        @Query("SELECT * FROM order_table WHERE productId = :productId")
        suspend fun get(productId: Int): OrderEntity?
}
