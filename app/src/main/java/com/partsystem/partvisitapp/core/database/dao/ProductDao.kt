package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(list: List<ProductEntity>)

    @Query("SELECT * FROM product_table ")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("DELETE FROM product_table")
    suspend fun clearProducts()

    @Query("SELECT * FROM product_table WHERE id = :id")
    fun getProductById(id: Int): LiveData<ProductEntity>

    @Query("SELECT * FROM product_table WHERE saleRastehId = :saleRastehId")
    fun getProductsByCategory(saleRastehId: Int): Flow<List<ProductEntity>>

    /*@Query("SELECT * FROM product_table WHERE subGroupId = :subGroupId")
    fun getProductsBySubGroup(subGroupId: Int): LiveData<List<ProductEntity>>*/

}


