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

    @Query("DELETE FROM SaleCenter")
    suspend fun clearSaleCenters()

    @Query("DELETE FROM SaleCenterAnbar")
    suspend fun clearSaleCenterAnbars()

    @Query("DELETE FROM SaleCenterUser")
    suspend fun clearSaleCenterUsers()

    @Query("""
    SELECT DISTINCT sc.*
    FROM SaleCenter AS sc
    LEFT JOIN InvoiceCategoryCenter AS icc ON icc.CenterId = sc.id
    WHERE icc.InvoiceCategoryId IS NULL 
       OR icc.InvoiceCategoryId = :invoiceCategoryId
""")
    fun getSaleCenters(invoiceCategoryId: Int): List<SaleCenterEntity>


    @Query("SELECT anbarId FROM SaleCenterAnbar WHERE saleCenterId = :saleCenterId LIMIT 1")
    suspend fun getActiveSaleCenterAnbar(saleCenterId: Int): Int?
}
