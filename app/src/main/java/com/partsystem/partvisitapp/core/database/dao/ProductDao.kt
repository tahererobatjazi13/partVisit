package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductFullData
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithRate
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


    @Transaction
    @Query(
        """
       SELECT  
        p.*,
        ad.rate AS actRate,
        ad.vatPercent AS actVatPercent,
        ad.tollPercent AS actTollPercent,
        (ad.rate + (ad.rate * ad.vatPercent) + (ad.rate * ad.tollPercent)) AS finalRate
    FROM product_table p
    INNER JOIN act_detail_table ad ON ad.productId = p.id
        WHERE (:groupProductId IS NULL OR p.saleGroupId = :groupProductId 
               OR p.saleGroupDetailId = :groupProductId 
               OR p.saleRastehId = :groupProductId)
        AND (:actId IS NULL OR ad.actId = :actId)
        ORDER BY p.code
    """
    )
    fun getProductsWithActDetails(groupProductId: Int?, actId: Int?): Flow<List<ProductWithPacking>>

    @Transaction
    @Query(
        """
        SELECT 
            p.id AS id,
            p.Code AS code,
            p.Name AS name,
            p.Description AS description,
            ad.Rate AS actRate,
            ad.Rate * ad.TollPercent AS toll,
            ad.Rate * ad.VatPercent AS vat,
            ad.TollPercent AS actTollPercent,
            ad.VatPercent AS actVatPercent,
            p.Unit2Id AS unit2Id,
            p.ConvertRatio AS convertRatio,
            p.CalculateUnit2Type AS calculateUnit2Type,
            ad.RateAfterVatAndToll AS finalRate,
            a.FileName AS fileName
        FROM product_table p
        INNER JOIN act_detail_table ad ON ad.ProductId = p.Id
        LEFT JOIN product_images_table a ON a.OwnerId = p.Id
        WHERE p.Id = :id AND ad.ActId = :actId
        LIMIT 1
    """
    )
    suspend fun getProductWithRate(id: Int, actId: Int): ProductWithPacking?

    /*
        @Transaction
        @Query("""
        SELECT p.*,
               ad.rate AS rate,
               ad.vatPercent AS vatPercent,
               ad.tollPercent AS tollPercent,
               ad.rate * ad.tollPercent AS toll,
               ad.rate * ad.vatPercent AS vat,
               ad.rateAfterVatAndToll AS rateAfterVatAndToll
        FROM product_table p
        INNER JOIN act_detail_table ad ON ad.productId = p.id
        WHERE (:groupProductId = '' OR
               p.saleGroupId = :groupProductId OR
               p.saleGroupDetailId = :groupProductId OR
               p.saleRastehId = :groupProductId)
          AND (:actId = '' OR ad.actId = :actId)
        ORDER BY p.code
    """)
        fun getProductsWithActDetails(groupProductId: Int?, actId: Int?): Flow<List<ProductWithPacking>>*/
}


