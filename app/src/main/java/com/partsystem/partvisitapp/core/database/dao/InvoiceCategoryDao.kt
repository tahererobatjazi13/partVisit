package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
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


    @Query("""
        SELECT ic.*
        FROM invoice_category_table ic
        LEFT JOIN invoice_category_detail_table icd
            ON icd.InvoiceCategoryId = ic.Id
        WHERE icd.UserId IS NULL OR icd.UserId = :userId
        ORDER BY Code ASC
    """)
     fun getInvoiceCategory(userId: Int): Flow<List<InvoiceCategoryEntity>>

}
