package com.partsystem.partvisitapp.core.database.dao

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
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FactorDao {

    @Update
    fun updateFactor(factor: FactorHeaderEntity)

    @Delete
    fun deleteFactor(factor: FactorHeaderEntity)

    @Query("SELECT * FROM FactorHeader WHERE id = :id")
    fun getFactorById(id: Int): FactorHeaderEntity?

    @Query("SELECT * FROM FactorDetail")
    fun getAllFactorDetail(): LiveData<List<FactorDetailEntity>>

    @Query("DELETE FROM FactorDetail WHERE productId = :productId")
    suspend fun deleteFactorDetail(productId: Int)

    @Query("DELETE FROM FactorHeader")
    suspend fun clearFactorHeader()

    @Query("DELETE FROM FactorDetail")
    suspend fun clearFactorDetails()

    @Query("DELETE FROM FactorDiscount")
    suspend fun clearFactorDiscount()

    @Query("DELETE FROM FactorGiftInfo")
    suspend fun clearFactorGiftInfo()


////////////////

    // Header
    @Query("UPDATE FactorHeader SET hasDetail = :hasDetail WHERE id = :factorId")
    suspend fun updateHasDetail(factorId: Int, hasDetail: Boolean)

    @Query("SELECT COUNT(*) FROM FactorDetail WHERE factorId = :factorId")
    suspend fun getDetailCountForFactor(factorId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorHeader(header: FactorHeaderEntity): Long

    @Query("SELECT * FROM FactorHeader WHERE id = :id LIMIT 1")
    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity>

    @Query("SELECT * FROM FactorHeader WHERE id = :localId LIMIT 1")
    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity?

    @Query("SELECT * FROM FactorHeader WHERE id = :factorId LIMIT 1")
    suspend fun getFactorHeaderById(factorId: Int): FactorHeaderEntity?

    @Update
    suspend fun updateFactorHeader(header: FactorHeaderEntity)

    @Update
    suspend fun updateHeader(header: FactorHeaderEntity)

    @Query("SELECT * FROM FactorHeader WHERE uniqueId = :uniqueId LIMIT 1")
    suspend fun getHeaderByUniqueId(uniqueId: String): FactorHeaderEntity?

    @Transaction
    suspend fun deleteFactor(factorId: Int) {
        // حذف تمام تخفیف‌های مرتبط با فاکتور (هم سطح ردیف و هم سطح فاکتور)
        deleteAllDiscountsByFactorId(factorId)

        // حذف جوایز
        deleteFactorGiftInfos(factorId)

        // حذف ردیف‌ها (اختیاری - CASCADE انجام می‌دهد)
        // deleteFactorDetails(factorId)

        // حذف هدر
        deleteFactorHeader(factorId)
    }

    @Query("DELETE FROM FactorDiscount WHERE factorId = :factorId AND factorDetailId IS NULL")
    suspend fun deleteFactorLevelDiscounts(factorId: Int)

    @Query("DELETE FROM FactorDiscount WHERE factorId = :factorId")
    suspend fun deleteAllDiscountsByFactorId(factorId: Int)

    @Query("DELETE FROM FactorGiftInfo WHERE factorId = :factorId")
    suspend fun deleteFactorGiftInfos(factorId: Int)

    @Query("DELETE FROM FactorHeader WHERE id = :factorId")
    suspend fun deleteFactorHeader(factorId: Int)

    // حذف تخفیف‌های سطح ردیف
    @Query("DELETE FROM FactorDiscount WHERE factorDetailId = :factorDetailId")
    suspend fun deleteDiscountsByFactorDetailId(factorDetailId: Int)

    // Detail
    @Query("SELECT MAX(Id) FROM FactorDetail")
    fun getMaxFactorDetailId(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(detail: FactorDetailEntity): Long

    @Delete
    suspend fun deleteDetail(detail: FactorDetailEntity)

    @Query(
        """
        SELECT * FROM FactorDetail 
        WHERE factorId = :factorId 
          AND productId = :productId 
          AND isGift = 0  
        LIMIT 1
    """
    )
    fun getFactorDetailByFactorIdAndProductId(
        factorId: Int,
        productId: Int
    ): Flow<FactorDetailEntity>


    @Query("SELECT * FROM FactorDiscount WHERE productId = :productId AND factorDetailId = :factorDetailId LIMIT 1")
    suspend fun getFactorDiscountByProductIdAndFactorDetailId(
        productId: Int,
        factorDetailId: Int
    ): FactorDiscountEntity?

    @Query(
        """
    SELECT COUNT(*) FROM FactorDetail 
    WHERE factorId = :factorId
     AND isGift = 0  
        LIMIT 1
    """
    )
    fun getFactorItemCount(factorId: Int): LiveData<Int>


    @Query(
        """
        SELECT * FROM FactorDetail 
        WHERE factorId = :factorId 
          AND isGift = 0  
        ORDER BY sortCode ASC
    """
    )
    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>>

    @Query(
        """
        SELECT * FROM FactorDetail 
        WHERE factorId = :factorId 
        ORDER BY sortCode ASC
    """
    )
    fun getAllFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>>


    // دریافت بعدین sortCode برای فاکتور جاری
    @Query("SELECT COALESCE(MAX(sortCode), 0) FROM FactorDetail WHERE factorId = :factorId")
    suspend fun getMaxSortCode(factorId: Int): Int


    @Query("UPDATE FactorDetail SET vat = :vat WHERE id = :id")
    suspend fun updateVat(id: Int, vat: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: FactorDetailEntity): Long

    @Update
    suspend fun update(detail: FactorDetailEntity)

    @Transaction
    suspend fun upsertFactorDetail(detail: FactorDetailEntity): Long {
        val existing = getDetailByFactorAndProduct(detail.factorId, detail.productId)
        return if (existing != null) {
            val updated = detail.copy(
                id = existing.id,
                sortCode = existing.sortCode
            )
            update(updated)
            existing.id.toLong()
        } else {
            val nextSort = getMaxSortCode(detail.factorId) + 1
            insert(detail.copy(id = 0, sortCode = nextSort))
        }
    }

    @Query(
        """
        SELECT * FROM FactorDetail 
        WHERE factorId = :factorId AND productId = :productId 
        LIMIT 1
    """
    )
    suspend fun getDetailByFactorAndProduct(
        factorId: Int,
        productId: Int
    ): FactorDetailEntity?


    @Query(
        """
    DELETE FROM FactorDetail
    WHERE factorId = :factorId AND productId = :productId
    """
    )
    suspend fun deleteByFactorAndProduct(
        factorId: Int,
        productId: Int
    )

    @Query(
        """
    SELECT
        fd.factorId,
        fd.productId,
        p.name AS productName,
        p.unitName AS unit1Name,
        pp.packingName AS packingName,
        pp.unit1Value AS unitPerPack,
        fd.id,
        fd.unit1Value,
        fd.unit2Value,
        fd.packingId,
        fd.packingValue,
        fd.isGift,
        fd.unit1Rate,
        fd.vat,
        fd.sortCode,
        COALESCE(SUM(fdi.price), 0) AS discountPrice  -- جمع تخفیف‌ها
    FROM FactorDetail fd
    LEFT JOIN Product p
        ON fd.productId = p.id
    LEFT JOIN ProductPacking pp
        ON fd.packingId = pp.packingCode
        AND fd.productId = pp.productId
    LEFT JOIN FactorDiscount fdi
        ON fd.id = fdi.factorDetailId
    WHERE fd.factorId = :factorId
    GROUP BY fd.id
    ORDER BY fd.sortCode
    """
    )
    fun getFactorDetailUi(factorId: Int): Flow<List<FactorDetailUiModel>>

    @Query("SELECT * FROM FactorDetail WHERE factorId = :factorId")
    suspend fun getFactorDetailsRaw(factorId: Int): List<FactorDetailEntity>

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
        fh.actId,
        fh.sabt,
        CASE 
            WHEN COUNT(fd.factorId) > 0 THEN 1 
            ELSE 0 
        END AS hasDetail
    FROM FactorHeader fh
    LEFT JOIN Customer c ON fh.customerId = c.id
    LEFT JOIN Pattern p ON fh.patternId = p.id
    LEFT JOIN FactorDetail fd ON fd.factorId = fh.id
    GROUP BY fh.id
    ORDER BY fh.id DESC
    """
    )
    fun getFactorHeaderDbList(): Flow<List<FactorHeaderDbModel>>


    @Query("SELECT * FROM FactorHeader WHERE id = :factorId LIMIT 1")
    suspend fun getHeaderByIdSuspend(factorId: Int): FactorHeaderEntity

    @Upsert
    suspend fun insertFactorDiscount(discount: FactorDiscountEntity)

    @Query("UPDATE FactorDiscount SET price = :price, discountPercent = :discountPercent WHERE id = :id")
    suspend fun updateFactorDiscount(id: Int, price: Double, discountPercent: Double)

    // Gift
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorGift(gifts: FactorGiftInfoEntity): Long

    @Query("SELECT * FROM FactorGiftInfo WHERE factorId = :factorId")
    suspend fun getFactorGifts(factorId: Int): List<FactorGiftInfoEntity>

    // تخفیف‌های سطح فاکتور (جایی که factorDetailId = NULL)
    @Query("SELECT * FROM FactorDiscount WHERE factorId = :factorId AND factorDetailId IS NULL")
    suspend fun getFactorLevelDiscounts(factorId: Int): List<FactorDiscountEntity>

    // تخفیف‌های سطح ردیف (جایی که factorDetailId مقدار دارد)
    @Query("SELECT * FROM FactorDiscount WHERE factorId = :factorId AND factorDetailId = :factorDetailId")
    suspend fun getDetailLevelDiscounts(
        factorId: Int,
        factorDetailId: Int
    ): List<FactorDiscountEntity>

    @Query("SELECT COUNT(*) FROM FactorDetail")
    fun getCount(): LiveData<Int>

    @Query(
        """
    SELECT SUM(Price) 
    FROM FactorDetail 
    WHERE factorId = :factorId 
      AND isGift = 0
    """
    )
    suspend fun getSumPrice(factorId: Int): Double?

    @Query(
        """
    SELECT SUM(Price) 
    FROM FactorDetail 
    WHERE factorId = :factorId 
      AND isGift = 0 
      AND productId IN (:productIds)
    """
    )
    suspend fun getSumPriceByProductIds(factorId: Int, productIds: List<Int>): Double?


    @Query(
        """
        SELECT SUM(Price) 
        FROM FactorDetail 
        WHERE factorId = :factorId 
          AND isGift = 0 
          AND (:filterByProducts = 0 OR productId IN (:productIds))
    """
    )
    suspend fun getSumPrice(
        factorId: Int,
        productIds: List<Int> = emptyList(),
        filterByProducts: Int = if (productIds.isEmpty()) 0 else 1
    ): Double?


    @Query(
        """
        SELECT SUM(CASE 
            WHEN :fieldName = 'Unit1Value' THEN unit1Value
            WHEN :fieldName = 'Unit2Value' THEN unit2Value
            WHEN :fieldName = 'PackingValue' THEN packingValue
            ELSE 0 
        END)
        FROM FactorDetail 
        WHERE factorId = :factorId 
          AND isGift = 0 
          AND (:filterByProducts = 0 OR productId IN (:productIds))
    """
    )
    suspend fun getSumField(
        factorId: Int,
        fieldName: String,
        productIds: List<Int> = emptyList(),
        filterByProducts: Int = if (productIds.isEmpty()) 0 else 1
    ): Double?


    @Query(
        """
        SELECT * 
        FROM FactorDetail 
        WHERE FactorId = :factorId 
          AND IsGift = 0
    """
    )
    suspend fun getNonGiftFactorDetails(factorId: Int): List<FactorDetailEntity>

    @Query(
        """
        SELECT * 
        FROM FactorDetail 
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
        FROM FactorDetail
        WHERE FactorId = :factorId
          AND IsGift = 0
    """
    )
    suspend fun getSumUnit1ValueByFactorId(factorId: Int): Double

    @Query("SELECT * FROM FactorDetail WHERE factorId = :factorId")
    suspend fun getDetailsByFactorId(factorId: Int): List<FactorDetailEntity>

    @Query("SELECT * FROM FactorDetail WHERE factorId = :factorId AND id = :detailId")
    suspend fun getDetailById(factorId: Int, detailId: Int): FactorDetailEntity?

    @Query("SELECT * FROM FactorDetail WHERE factorId = :factorId AND productId IN (:productIds)")
    suspend fun getDetailsByFactorAndProducts(
        factorId: Int,
        productIds: List<Int>
    ): List<FactorDetailEntity>

    @Transaction
    @Query("SELECT * FROM Discount WHERE id = :discountId")
    suspend fun getDiscountWithStairs(discountId: Int): DiscountEntity?

    @Query(
        """
        SELECT pd.actId
        FROM PatternDetail AS pd
        INNER JOIN Act AS a ON a.id = pd.actId
        WHERE pd.patternId = :patternId
          AND a.kind = :kind
          AND pd.isDefault = 1
        LIMIT 1
        """
    )
    suspend fun getPatternDetailActId(patternId: Int, kind: Int): Int?

    @Query("SELECT * FROM DiscountEshantyun WHERE discountId = :discountId")
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
        FROM FactorDetail
        WHERE FactorId = :factorId
        AND ProductId IN (:productIds)  AND IsGift = 0 
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
    FROM FactorDetail
    WHERE FactorId = :factorId
       AND productId = :productId   AND IsGift = 0 
    ORDER BY SortCode ASC
"""
    )
    suspend fun getFactorDetail(
        factorId: Int,
        productId: Int
    ): List<VwFactorDetail>


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


    @Query(
        """
    SELECT IFNULL(SUM(fd.Price), 0) 
    FROM FactorDiscount fd
    JOIN Discount d ON fd.DiscountId = d.Id
    WHERE fd.FactorDetailId = :detailId 
    AND d.Kind = 0  -- DiscountKind.Discount
"""
    )
    suspend fun getTotalDiscountForDetail(detailId: Int): Double?

    @Query(
        """
    SELECT IFNULL(SUM(fd.Price), 0) 
    FROM FactorDiscount fd
    JOIN Discount d ON fd.DiscountId = d.Id
    WHERE fd.FactorDetailId = :detailId 
    AND d.Kind = 1  -- DiscountKind.Addition
"""
    )
    suspend fun getTotalAdditionForDetail(detailId: Int): Double?

    // جمع تخفیف‌های سطح فاکتور
    @Query("SELECT SUM(price) FROM FactorDiscount WHERE factorId = :factorId AND factorDetailId IS NULL")
    suspend fun getTotalFactorLevelDiscount(factorId: Int): Double?

    // جمع تخفیف‌های سطح ردیف
    @Query("SELECT SUM(price) FROM FactorDiscount WHERE factorDetailId IN (SELECT id FROM FactorDetail WHERE factorId = :factorId)")
    suspend fun getTotalProductLevelDiscount(factorId: Int): Double?

}
