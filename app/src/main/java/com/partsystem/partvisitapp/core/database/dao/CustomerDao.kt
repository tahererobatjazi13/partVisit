package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    // درج لیست مشتری‌ها (برای سینک با API)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    // دریافت همه مشتری‌ها
    @Query("SELECT * FROM customer_table")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    // دریافت مشتری بر اساس ID
    @Query("SELECT * FROM customer_table WHERE id = :customerId LIMIT 1")
    fun getCustomerById(customerId: Int): LiveData<CustomerEntity>

    // درج یک مشتری
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    // آپدیت اطلاعات مشتری
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    // حذف مشتری
    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // جستجوی مشتری بر اساس نام
    @Query("SELECT * FROM customer_table WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchCustomer(keyword: String): List<CustomerEntity>

    // حذف همه مشتری‌ها (برای زمانی که API سینک کامل می‌کنی)
    @Query("DELETE FROM customer_table")
    suspend fun clearAll()
}
