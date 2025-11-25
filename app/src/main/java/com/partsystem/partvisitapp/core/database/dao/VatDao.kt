package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.VatDetailEntity
import com.partsystem.partvisitapp.core.database.entity.VatEntity
import com.partsystem.partvisitapp.core.database.entity.VatWithDetails

@Dao
interface VatDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVat(list: List<VatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVatDetails(details: List<VatDetailEntity>)

    @Transaction
    @Query("SELECT * FROM vat_table")
    suspend fun getVatWithDetails(): List<VatWithDetails>

    @Query("DELETE FROM vat_table")
    suspend fun clearVat()

    @Query("DELETE FROM vat_detail_table")
    suspend fun clearVatDetails()
}
