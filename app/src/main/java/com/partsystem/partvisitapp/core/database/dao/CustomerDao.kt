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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    // کنترل برنامه ویزیت غیرفعال

    @Query(
        """
        SELECT c.* 
        FROM customer_table c
        INNER JOIN assign_direction_customer_table adc
            ON c.id = adc.customerId 
           AND adc.saleCenterId = c.saleCenterId 
        WHERE c.saleCenterId = :saleCenterId 
          AND adc.tafsiliId = :visitorId
        ORDER BY c.code ASC
    """
    )
     fun getCustomersWithoutVisitSchedule(
        saleCenterId: Int,
        visitorId: Int
    ): Flow<List<CustomerEntity>>



    /*
    @Query(
        """
    SELECT c.*
    FROM customer_table c
    INNER JOIN assign_direction_customer_table adc
          ON c.id = adc.customerId
         AND adc.saleCenterId = c.saleCenterId
    WHERE c.saleCenterId = :saleCenterId
      AND adc.tafsiliId = :visitorId
"""
    )
    fun getCustomersWithoutVisitSchedule(
        saleCenterId: Int,
        visitorId: Int
    ): Flow<List<CustomerEntity>>*/


    // کنترل برنامه ویزیت فعال
    @Query(
        """
    SELECT c.*
    FROM customer_table c
    INNER JOIN visit_schedule_detail_table vsd 
           ON c.id = vsd.customerId
    INNER JOIN visit_schedule_table vs
           ON vs.id = vsd.visitScheduleId
    INNER JOIN assign_direction_customer_table adc
           ON c.id = adc.customerId
          AND adc.tafsiliId = vs.visitorId
          AND adc.saleCenterId = c.saleCenterId
    WHERE c.saleCenterId = :saleCenterId
      AND vs.visitorId = :visitorId
      AND vs.persianDate = :persianDate
    ORDER BY c.code ASC
"""
    )
    fun getCustomersByVisitSchedule(
        saleCenterId: Int,
        visitorId: Int,
        persianDate: String
    ): Flow<List<CustomerEntity>>


    // دریافت همه مشتری‌ها
    @Query("SELECT * FROM customer_table")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    //  نسخه suspend برای coroutine   //  دریافت مشتری بر اساس ID
    @Query("SELECT * FROM customer_table WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): CustomerEntity?

    // نسخه LiveData برای مستقیم observe  //  دریافت مشتری بر اساس ID
    @Query("SELECT * FROM customer_table WHERE id = :id LIMIT 1")
    fun getCustomerByIdLive(id: Int): LiveData<CustomerEntity>

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
    suspend fun clearCustomers()
}
