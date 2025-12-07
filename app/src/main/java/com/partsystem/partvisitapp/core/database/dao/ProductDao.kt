package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithRateDb
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

/*
        @Query("""
        SELECT 
            p.id AS productId,
            p.code,
            p.name,
            p.description,
            ad.rate,
            p.unit2Id,
            p.convertRatio,
            p.calculateUnit2Type,
            (ad.rate * ad.tollPercent) AS toll,
            (ad.rate * ad.vatPercent) AS vat,
            ad.rateAfterVatAndToll,
            ad.tollPercent,
            ad.vatPercent,
            img.fileName
        FROM product_table p
        INNER JOIN act_detail_table ad ON ad.productId = p.id
        LEFT JOIN product_images_table img ON img.ownerId = p.id
        WHERE (:groupId IS NULL OR
              p.saleGroupId = :groupId OR
              p.saleGroupDetailId = :groupId OR
              p.saleRastehId = :groupId)
          AND ad.actId = :actId
        ORDER BY p.code
    """)
        suspend fun getProductsWithRate(
            actId: Int,
            groupId: Int?
        ): List<ProductWithRateDb>

        // گرفتن بسته‌بندی‌های محصول
        @Query("""
        SELECT * 
        FROM product_packing_table 
        WHERE productId = :productId
        ORDER BY isDefault DESC
    """)
        suspend fun getPacking(productId: Int): List<ProductPackingEntity>
*/


    @Transaction
    @Query("""
        SELECT p.*, ad.rate AS rate, ad.vatPercent AS vatPercent, ad.tollPercent AS tollPercent
        FROM product_table p
        INNER JOIN act_detail_table ad ON ad.productId = p.id
        WHERE (:groupProductId IS NULL OR p.saleGroupId = :groupProductId 
               OR p.saleGroupDetailId = :groupProductId 
               OR p.saleRastehId = :groupProductId)
        AND (:actId IS NULL OR ad.actId = :actId)
        ORDER BY p.code
    """)
    fun getProductsWithActDetails(groupProductId: Int?, actId: Int?): Flow<List<ProductWithPacking>>

}

data class ProductWithPacking(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val packings: List<ProductPackingEntity>
)

