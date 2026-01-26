package com.partsystem.partvisitapp.core.database.dao

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEshantyunsEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.feature.create_order.model.VwFactorDetail
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FactorDao {


    @Update
    fun updateFactor(factor: FactorHeaderEntity)

    @Delete
    fun deleteFactor(factor: FactorHeaderEntity)

    @Query("SELECT * FROM factor_header_table WHERE id = :id")
    fun getFactorById(id: Int): FactorHeaderEntity?

    @Query("SELECT * FROM factor_header_table")
    suspend fun getAllFactors(): List<FactorHeaderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(details: List<FactorDetailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(detail: FactorDetailEntity)

    @Update
    suspend fun updateFactorDetail(item: FactorDetailEntity)

    @Query("SELECT * FROM factor_detail_table")
    fun getAllFactorDetail(): LiveData<List<FactorDetailEntity>>

    @Query("DELETE FROM factor_detail_table WHERE productId = :productId")
    suspend fun deleteFactorDetail(productId: Int)

    @Query("DELETE FROM factor_header_table")
    suspend fun clearFactorHeader()

    @Query("DELETE FROM factor_detail_table")
    suspend fun clearFactorDetails()

    @Query("DELETE FROM factor_discount_table")
    suspend fun clearFactorDiscount()

    @Query("DELETE FROM factor_gift_info_table")
    suspend fun clearFactorGiftInfo()


////////////////

    // Header

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorHeader(header: FactorHeaderEntity): Long

    @Query("SELECT * FROM factor_header_table WHERE id = :id LIMIT 1")
    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity>

    @Query("SELECT * FROM factor_header_table WHERE id = :localId LIMIT 1")
    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity?


    @Update
    suspend fun updateFactorHeader(header: FactorHeaderEntity)

    @Update
    suspend fun updateHeader(header: FactorHeaderEntity)

    @Query("SELECT * FROM factor_header_table WHERE uniqueId = :uniqueId LIMIT 1")
    suspend fun getHeaderByUniqueId(uniqueId: String): FactorHeaderEntity?


    @Query("SELECT * FROM factor_header_table ORDER BY id DESC ")
    fun getAllHeaders(): Flow<List<FactorHeaderEntity>>

    /*   @Query("DELETE FROM factor_header_table WHERE uniqueId = :uniqueId")
       suspend fun deleteHeaderByUniqueId(uniqueId: String)
       */

    @Query("DELETE FROM factor_header_table WHERE id = :factorId")
    suspend fun deleteFactor(factorId: Int)


    // Detail


    @Query("SELECT MAX(Id) FROM factor_detail_table")
     fun getMaxFactorDetailId():  LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(detail: FactorDetailEntity): Long


    @Delete
    suspend fun deleteDetail(detail: FactorDetailEntity)

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId")
    suspend fun getDetailsForHeader(factorId: String): List<FactorDetailEntity>

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId AND productId = :productId")
    fun getFactorDetailByFactorIdAndProductId(factorId: Int, productId: Int): Flow<FactorDetailEntity>


    @Query("SELECT * FROM factor_discount_table WHERE productId = :productId AND factorDetailId = :factorDetailId LIMIT 1")
    suspend fun getFactorDiscountByProductIdAndFactorDetailId(productId: Int, factorDetailId: Int): FactorDiscountEntity?

    @Query(
        """
    SELECT COUNT(*) FROM factor_detail_table 
    WHERE factorId = :factorId
    """
    )
    fun getFactorItemCount(factorId: Int): LiveData<Int>


    @Query("SELECT * FROM factor_detail_table ")
    fun getAllFactorDetails( ): Flow<List<FactorDetailEntity>>

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId")
    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>>

    @Upsert
    suspend fun upsert(detail: FactorDetailEntity)

    @Query(
        """
    DELETE FROM factor_detail_table
    WHERE factorId = :factorId AND productId = :productId
    """
    )
    suspend fun deleteByFactorAndProduct(
        factorId: Int,
        productId: Int
    )

    @Query(
        """
    DELETE FROM factor_detail_table 
    WHERE factorId = :factorId
    """
    )
    suspend fun clearFactor(factorId: Int)

    @Query(
        """
    SELECT 
        fd.factorId,
        fd.productId,
        p.name AS productName,
        p.unitName AS unit1Name,
        pp.packingName AS packingName,
        pp.unit1Value AS unitPerPack,
        fdi.price AS discountPrice,
        fd.id,
        fd.unit1Value,
        fd.unit2Value,
        fd.packingValue,
        fd.isGift,
        fd.unit1Rate,
        fd.vat,
        fdi.price
        
    FROM factor_detail_table fd
    LEFT JOIN product_table p 
        ON fd.productId = p.id
    LEFT JOIN product_packing_table pp 
        ON fd.packingId = pp.packingCode
       AND fd.productId = pp.productId
        LEFT JOIN factor_discount_table fdi 
        ON fd.id = fdi.factorDetailId
    WHERE fd.factorId = :factorId
    ORDER BY fd.sortCode
"""
    )
    fun getFactorDetailUi(factorId: Int): Flow<List<FactorDetailUiModel>>


    @Query(
        """
    SELECT 
        fh.id AS factorId,
        fh.customerId,
        c.name AS customerName,
        fh.patternId,
        p.name AS patternName,
        fh.persianDate,
        fh.createTime,
        fh.finalPrice,
        CASE 
            WHEN COUNT(fd.factorId) > 0 THEN 1 
            ELSE 0 
        END AS hasDetail
    FROM factor_header_table fh
    LEFT JOIN customer_table c ON fh.customerId = c.id
    LEFT JOIN pattern_table p ON fh.patternId = p.id
    LEFT JOIN factor_detail_table fd ON fd.factorId = fh.id
    GROUP BY fh.id
    ORDER BY fh.id DESC
    """
    )
    fun getFactorHeaderUiList(): Flow<List<FactorHeaderUiModel>>

    // Discounts

  /*  @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDiscount(discount: FactorDiscountEntity): Long
*/
    @Upsert
    suspend fun insertFactorDiscount(discount: FactorDiscountEntity)



//      @Query("SELECT * FROM factor_discount_table WHERE FactorId = :factorId")
//       suspend fun getFactorDiscounts(factorId: Int): List<FactorDiscountEntity>
//

    /*  @Query("SELECT * FROM factor_discount_table WHERE factorId = :factorId")
      suspend fun getDiscountsForHeader(factorId: String): List<FactorDiscountEntity>

      @Query("DELETE FROM factor_discount_table WHERE factorId = :factorId")
      suspend fun deleteDiscountsForHeader(factorId: String)*/

    // Gift
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorGift(discount: FactorGiftInfoEntity): Long


    @Query("SELECT * FROM factor_gift_info_table WHERE factorId = :factorId")
    suspend fun getFactorGifts(factorId: Int): List<FactorGiftInfoEntity>

    //@Query("SELECT SUM(Unit1Value) FROM factor_detail_table WHERE FactorId = :factorId AND ProductId = :productId AND IsGift = 0")

    @Query("SELECT * FROM factor_discount_table WHERE factorId = :factorId AND factorDetailId = :factorDetailId")
    suspend fun getFactorDiscounts(factorId: Int, factorDetailId: Int): List<FactorDiscountEntity>

    @Query("SELECT * FROM factor_discount_table WHERE factorId = :factorId AND factorDetailId = :factorDetailId")
    fun getFactorDiscountsLive(factorId: Int, factorDetailId: Int): Flow<List<FactorDiscountEntity>>
    /*
        @Query("DELETE FROM factor_gift_info_table WHERE id = :headerId")
        suspend fun deleteHeader(headerId: Long)*/

    @Query("SELECT COUNT(*) FROM factor_detail_table")
     fun getCount(): LiveData<Int>

    @Query(
        """
        SELECT SUM(Price) 
        FROM factor_detail_table 
        WHERE FactorId = :factorId 
          AND IsGift = 0 
          AND ProductId IN (:productIds)
    """
    )
    suspend fun getSumPriceByProductIds(factorId: Int, productIds: List<Int>): Double?


    @Query(
        """
        SELECT * 
        FROM factor_detail_table 
        WHERE FactorId = :factorId 
          AND IsGift = 0
    """
    )
    suspend fun getNonGiftFactorDetails(factorId: Int): List<FactorDetailEntity>

    @Query(
        """
        SELECT * 
        FROM factor_detail_table 
        WHERE FactorId = :factorId 
          AND IsGift = 0 
          AND ProductId IN (:productIds)
    """
    )
    suspend fun getNonGiftFactorDetailsByProductIds(
        factorId: Int,
        productIds: List<Int>
    ): List<FactorDetailEntity>

    @Query(
        """
        SELECT IFNULL(SUM(Unit1Value), 0.0)
        FROM factor_detail_table
        WHERE FactorId = :factorId
          AND IsGift = 0
    """
    )
    suspend fun getSumUnit1ValueByFactorId(factorId: Int): Double


    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId")
    suspend fun getDetailsByFactorId(factorId: Int): List<FactorDetailEntity>

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId AND id = :detailId")
    suspend fun getDetailById(factorId: Int, detailId: Int): FactorDetailEntity?

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId AND productId IN (:productIds)")
    suspend fun getDetailsByFactorAndProducts(
        factorId: Int,
        productIds: List<Int>
    ): List<FactorDetailEntity>

    @Transaction
    @Query("SELECT * FROM discounts_table WHERE id = :discountId")
    suspend fun getDiscountWithStairs(discountId: Int): DiscountEntity?

    @Query(
        """
        SELECT pd.actId
        FROM pattern_details_table AS pd
        INNER JOIN act_table AS a ON a.id = pd.actId
        WHERE pd.patternId = :patternId
          AND a.kind = :kind
          AND pd.isDefault = 1
        LIMIT 1
        """
    )
    suspend fun getPatternDetailActId(patternId: Int, kind: Int): Int?

    @Query("SELECT * FROM discount_eshantyuns_table WHERE discountId = :discountId")
    suspend fun getEshantyunsByDiscountId(discountId: Int): List<DiscountEshantyunsEntity>


    @Query(
        """
        SELECT 
            FactorId,
            Id,
            SortCode,
            ProductId,
            Unit1Value,
            Unit2Value,
            Price,
            PackingId,
            PackingValue,
            IsGift
        FROM factor_detail_table
        WHERE FactorId = :factorId
        AND ProductId IN (:productIds)
        ORDER BY SortCode ASC
    """
    )
    suspend fun getFactorDetailByProductIds(
        factorId: Int,
        productIds: List<Int>
    ): List<VwFactorDetail>


    @Query(
        """
    SELECT 
        FactorId,
        Id,
        SortCode,
        ProductId,
        Unit1Value,
        Unit2Value,
        Price,
        PackingId,
        PackingValue,
        IsGift
    FROM factor_detail_table
    WHERE FactorId = :factorId
    ORDER BY SortCode ASC
"""
    )
    suspend fun getFactorDetail(
        factorId: Int
    ): List<VwFactorDetail>

    @Query("SELECT IFNULL(MAX(SortCode), 0) FROM factor_detail_table WHERE FactorId = :factorId")
    suspend fun getMaxSortCode(factorId: Int): Int


    @Transaction
    suspend fun insertFactorWithDiscountAndGifts(
        detail: FactorDetailEntity,
        discount: FactorDiscountEntity,
        gifts: List<FactorGiftInfoEntity>
    ) {
        insertFactorDetail(detail)
        insertFactorDiscount(discount)
        gifts.forEach { insertFactorGift(it) }
    }
}
