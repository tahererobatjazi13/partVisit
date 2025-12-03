package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.SaleCenterAnbarEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterUserEntity

@Dao
interface SaleCenterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleCenter(center: SaleCenterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnbars(anbars: List<SaleCenterAnbarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<SaleCenterUserEntity>)

    @Transaction
    suspend fun insertFull(center: SaleCenterEntity, anbars: List<SaleCenterAnbarEntity>, users: List<SaleCenterUserEntity>) {
        insertSaleCenter(center)
        insertAnbars(anbars)
        insertUsers(users)
    }

    @Query("SELECT * FROM sale_center_table")
    suspend fun getAllCenters(): List<SaleCenterEntity>

    @Query("SELECT * FROM sale_center_anbar_table WHERE saleCenterId = :centerId")
    suspend fun getAnbars(centerId: Int): List<SaleCenterAnbarEntity>

    @Query("SELECT * FROM sale_center_user_table WHERE saleCenterId = :centerId")
    suspend fun getUsers(centerId: Int): List<SaleCenterUserEntity>

    @Query("DELETE FROM sale_center_table")
    suspend fun clearSaleCenters()

    @Query("DELETE FROM sale_center_anbar_table")
    suspend fun clearAnbars()

    @Query("DELETE FROM sale_center_user_table")
    suspend fun clearUsers()

    @Query("""
    SELECT DISTINCT sc.*
    FROM sale_center_table AS sc
    LEFT JOIN invoice_category_center_table AS icc ON icc.CenterId = sc.id
    WHERE icc.InvoiceCategoryId IS NULL 
       OR icc.InvoiceCategoryId = :invoiceCategoryId
""")
    fun getSaleCenters(invoiceCategoryId: Int): List<SaleCenterEntity>


    @Query("SELECT anbarId FROM sale_center_anbar_table WHERE saleCenterId = :saleCenterId LIMIT 1")
    suspend fun getActiveSaleCenterAnbar(saleCenterId: Int): Int?
}
