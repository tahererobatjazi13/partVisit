package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.DiscountCustomersEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEshantyunsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountFull
import com.partsystem.partvisitapp.core.database.entity.DiscountGiftsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountGroupsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductKindsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductKindInclusionsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountUsersEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity

@Dao
interface DiscountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscounts(discounts: List<DiscountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountEshantyuns(eshantyuns: List<DiscountEshantyunsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountGifts(gifts: List<DiscountGiftsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountGroups(groups: List<DiscountGroupsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountProductKindInclusions(productKindInclusions: List<DiscountProductKindInclusionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountProductKinds(productKinds: List<DiscountProductKindsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountProducts(products: List<DiscountProductsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountStairs(stairs: List<DiscountStairsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountUsers(users: List<DiscountUsersEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountCustomers(customers: List<DiscountCustomersEntity>)

    @Query("SELECT * FROM discounts_table")
    suspend fun getAllDiscounts(): List<DiscountEntity>

    @Query("DELETE FROM discounts_table")
    suspend fun clearDiscounts()

   //AND ApplyKind = :applyKind
    @Transaction
    @Query(
        """
        SELECT * FROM discounts_table 
        WHERE FormType = 1 
          
          AND (ToDate IS NULL OR ToDate = '' OR ToDate >= :toDate)
          AND PersianBeginDate <= :persianDate
    """
    )
    suspend fun getDiscounts(
       // applyKind: Int,
        toDate: String,
        persianDate: String
    ): List<DiscountEntity>

    @Query("SELECT * FROM discounts_table WHERE Id = :id")
    suspend fun getDiscount(id: Int): DiscountEntity?

    @Transaction
    @Query(
        """
        SELECT DISTINCT p.Id 
        FROM discount_product_kind_inclusion_table AS g 
        INNER JOIN product_table AS p ON p.ProductKindId = g.ProductKindId 
        WHERE g.DiscountId = :discountId AND p.Id IN (:productIds)
    """
    )
    suspend fun getProductMatchProductKindInclusion(
        discountId: Int,
        productIds: List<Int>
    ): List<Int>

    // مشابه برای DiscountGroup (در صورت نیاز)
    @Transaction
    @Query(
        """
        SELECT DISTINCT p.Id 
        FROM discount_groups_table AS g 
        INNER JOIN product_table AS p ON p.groupProductId = g.GroupId 
        WHERE g.DiscountId = :discountId AND p.Id IN (:productIds)
    """
    )
    suspend fun getProductMatchDiscountGroup(
        discountId: Int,
        productIds: List<Int>
    ): List<Int>

    /*
        @Query(
            """
            SELECT *
            FROM discounts_table
            WHERE formType = 1
              AND applyKind = :applyKind
              AND (toDate IS NULL OR toDate = '' OR toDate >= :toDate)
              AND persianBeginDate <= :persianBeginDate
        """
        )
        suspend fun getDiscounts(
            applyKind: Int,
            toDate: String,
            persianBeginDate: String
        ): List<DiscountEntity>
    */


    @Transaction
    @Query("SELECT * FROM discount_eshantyuns_table WHERE DiscountId = :discountId")
    suspend fun getDiscountEshantyun(discountId: Int): List<DiscountEshantyunsEntity>

    @Transaction
    @Query("SELECT * FROM discount_gift_table WHERE DiscountId = :discountId")
    suspend fun getDiscountGift(discountId: Int): List<DiscountGiftsEntity>

    @Transaction
    @Query("SELECT * FROM discount_groups_table WHERE DiscountId = :discountId")
    suspend fun getDiscountGroup(discountId: Int): List<DiscountGroupsEntity>

    @Transaction
    @Query("SELECT * FROM discount_stairs_table WHERE DiscountId = :discountId")
    suspend fun getDiscountStair(discountId: Int): List<DiscountStairsEntity>

    @Transaction
    @Query("SELECT * FROM discount_products_table WHERE DiscountId = :discountId")
    suspend fun getDiscountProducts(discountId: Int): List<DiscountProductsEntity>

    @Transaction
    @Query("SELECT * FROM discount_product_kind_inclusion_table WHERE DiscountId = :discountId")
    suspend fun getDiscountProductKindInclusion(discountId: Int): List<DiscountProductKindInclusionsEntity>

    @Transaction
    @Query("SELECT * FROM discount_product_kind_table WHERE DiscountId = :discountId")
    suspend fun getDiscountProductKind(discountId: Int): List<DiscountProductKindsEntity>

    @Transaction
    @Query("SELECT * FROM discount_user_table WHERE DiscountId = :discountId")
    suspend fun getDiscountUser(discountId: Int): List<DiscountUsersEntity>


    //AND ApplyKind = :applyKind
    @Transaction
    @Query(
        """
        SELECT * FROM discounts_table 
        WHERE FormType = 1 

          AND (ToDate IS NULL OR ToDate = '' OR ToDate >= :toDate)
          AND PersianBeginDate <= :persianDate
    """
    )
    suspend fun getDiscountsWithDetails(
       // applyKind: Int,
        toDate: String,
        persianDate: String
    ): List<DiscountFull>


    @Query("SELECT COUNT(*) FROM factor_discount_table WHERE id = :factorId")
    suspend fun getFactorDiscountCountByFactorId(factorId: Int): Int

    @Query("SELECT COUNT(*) FROM factor_discount_table WHERE FactorDetailId = :factorDetailId")
    suspend fun getFactorDiscountCountByFactorDetailId(factorDetailId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDiscount(discount: FactorDiscountEntity)

    @Query("SELECT SUM(Unit1Value) FROM factor_detail_table WHERE FactorId = :factorId AND ProductId IN (:productIds) AND IsGift = 0")
    suspend fun getSumUnit1ValueByProductIds(factorId: Int, productIds: List<Int>): Double

    @Query("SELECT SUM(Unit1Value) FROM factor_detail_table WHERE FactorId = :factorId AND ProductId = :productId AND IsGift = 0")
    suspend fun getSumUnit1ValueByProduct(factorId: Int, productId: Int): Double

    @Query("SELECT ProductId FROM factor_detail_table WHERE FactorId = :factorId AND IsGift = 0")
    suspend fun getFactorProductIds(factorId: Int): List<Int>

    @Query("SELECT DiscountId FROM factor_discount_table WHERE id = :factorId AND FactorDetailId = :factorDetailId")
    suspend fun getAppliedDiscountIds(factorId: Int, factorDetailId: Int): List<Int>

    @Query("SELECT MAX(Id) FROM factor_gift_info_table")
    suspend fun getMaxFactorGiftInfoId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorGiftInfo(info: FactorGiftInfoEntity)


    @Query(
        """
    SELECT IFNULL(SUM(Price), 0)
    FROM (
        SELECT SUM(fd.Price) * dpk.DiscountPercent / 100.0 AS Price
        FROM factor_detail_table fd
        INNER JOIN product_table p ON p.Id = fd.ProductId
        INNER JOIN discount_product_kind_table dpk ON dpk.DiscountId = :discountId
        WHERE fd.FactorId = :factorId
          AND p.ProductKindId IS NOT NULL
        GROUP BY dpk.FromProductKind, dpk.ToProductKind, dpk.MinPrice, dpk.DiscountPercent
        HAVING SUM(fd.Price) >= dpk.MinPrice
           AND dpk.FromProductKind <= COUNT(DISTINCT p.ProductKindId)
           AND dpk.ToProductKind >= COUNT(DISTINCT p.ProductKindId)
    )
"""
    )
    suspend fun getDiscountByProductKind(discountId: Int, factorId: Int): Double


    @Query(
        """
        SELECT * FROM discount_gift_table
        WHERE DiscountID = :discountId
          AND :allPrice BETWEEN FromPrice AND ToPrice
        LIMIT 1
    """
    )
    suspend fun getDiscountGift(discountId: Int, allPrice: Double): DiscountGiftsEntity?


    @Query(
        """
        SELECT 
            CASE 
                WHEN d.PaymentKind = 1 THEN
                    CASE 
                        WHEN d.ExecuteKind = 0 THEN ds.Price
                        ELSE 
                            CAST(
                                (CASE ds.UnitKind 
                                    WHEN 0 THEN fdAgg.Unit1Value 
                                    WHEN 1 THEN fdAgg.Unit2Value 
                                    WHEN 2 THEN fdAgg.PackingValue 
                                END) / NULLIF(ds.Ratio, 0) 
                            AS INTEGER) * ds.Price
                    END
                ELSE
                    CASE 
                        WHEN d.ExecuteKind = 0 THEN :price * ds.Price / 100.0
                        ELSE 
                            :price * 
                            CAST(
                                (CASE ds.UnitKind 
                                    WHEN 0 THEN fdAgg.Unit1Value 
                                    WHEN 1 THEN fdAgg.Unit2Value 
                                    WHEN 2 THEN fdAgg.PackingValue 
                                END) / NULLIF(ds.Ratio, 0) 
                            AS INTEGER) * ds.Price / 100.0
                    END
            END AS Price
        FROM discount_stairs_table ds
        INNER JOIN discounts_table d ON ds.DiscountId = d.Id
        INNER JOIN (
            SELECT 
                SUM(fd.Unit1Value) AS Unit1Value,
                SUM(fd.Unit2Value) AS Unit2Value,
                SUM(fd.PackingValue) AS PackingValue
            FROM factor_detail_table fd
            WHERE fd.FactorId = :factorId
              AND fd.IsGift = 0
              AND (:factorDetailId IS NULL OR fd.Id = :factorDetailId)
              AND (:productIds IS NULL OR fd.ProductId IN (:productIds))
        ) fdAgg ON ds.DiscountId = :discountId
        WHERE ds.DiscountId = :discountId
          AND (
                (ds.UnitKind = 0 AND fdAgg.Unit1Value BETWEEN ds.FromPrice AND ds.ToPrice)
             OR (ds.UnitKind = 1 AND fdAgg.Unit2Value BETWEEN ds.FromPrice AND ds.ToPrice)
             OR (ds.UnitKind = 2 AND fdAgg.PackingValue BETWEEN ds.FromPrice AND ds.ToPrice)
          )
        LIMIT 1
    """
    )
    suspend fun getCalculateDiscountByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>?,
        factorDetailId: Int?,
        price: Double
    ): Double?

    /**
     * دریافت اولین DiscountStair معتبر برای یک discountId و یک مقدار قیمت
     * که در بازه‌ی [FromPrice, ToPrice] قرار دارد.
     */
    @Query(
        """
        SELECT * FROM discount_stairs_table
        WHERE DiscountId = :discountId
          AND :price BETWEEN FromPrice AND ToPrice
        LIMIT 1
    """
    )
    suspend fun getDiscountStairByPrice(discountId: Int, price: Double): DiscountStairsEntity?

}
