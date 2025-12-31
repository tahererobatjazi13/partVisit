package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEshantyunEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity

@Dao
interface DiscountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscounts(discounts: List<DiscountEntity>)

    @Query("SELECT * FROM discounts_table")
    suspend fun getAllDiscounts(): List<DiscountEntity>

    @Query("DELETE FROM discounts_table")
    suspend fun clearDiscounts()

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

    @Query("SELECT * FROM discounts_table WHERE Id = :id")
    suspend fun getDiscount(id: Int): DiscountEntity?


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



}
