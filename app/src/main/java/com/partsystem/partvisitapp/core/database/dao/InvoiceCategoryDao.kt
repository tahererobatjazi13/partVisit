package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<InvoiceCategoryEntity>)

    @Query("SELECT * FROM invoice_category_table")
    fun getAllInvoiceCategory(): Flow<List<InvoiceCategoryEntity>>

    @Query("DELETE FROM invoice_category_table")
    suspend fun clearInvoiceCategory()
}
