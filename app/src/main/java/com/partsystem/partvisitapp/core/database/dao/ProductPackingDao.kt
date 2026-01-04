package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

@Dao
interface ProductPackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packings: List<ProductPackingEntity>)

    @Query("SELECT * FROM product_packing_table")
    suspend fun getAll(): List<ProductPackingEntity>

    @Query("DELETE FROM product_packing_table")
    suspend fun clearProductPacking()

    @Query("SELECT * FROM product_packing_table WHERE productId = :productId")
    suspend fun getPackingsByProductId(productId: Int): List<ProductPackingEntity>

    @Query(
        """
        SELECT * FROM product_packing_table
        WHERE packingId = :packingId
        AND productId = :productId
    """
    )
    suspend fun getPackingByPackingIdAndProductId(
        packingId: Int,
        productId: Int
    ): List<ProductPackingEntity>
}
