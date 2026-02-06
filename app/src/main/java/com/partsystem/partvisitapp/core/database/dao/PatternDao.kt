package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.PatternDetailEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PatternEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PatternEntity)

    @Query("SELECT * FROM Pattern")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM Pattern WHERE id = :id")
    suspend fun getById(id: Int): PatternEntity?

    @Query("DELETE FROM Pattern")
    suspend fun clearPatterns()

    // 1) فیلتر مشتری و CustomerFilterKind
    @Query(
        """
        SELECT DISTINCT p.id
        FROM Pattern p
        LEFT JOIN PatternDetail pd 
              ON pd.kind = 1 AND pd.patternId = p.id
        LEFT JOIN (
            SELECT 
                SUM(CASE WHEN customerFilterKind = 1 THEN 1 ELSE 0 END) AS CK,
                SUM(CASE WHEN customerFilterKind = 2 THEN 1 ELSE 0 END) AS CD,
                SUM(CASE WHEN customerFilterKind = 3 THEN 1 ELSE 0 END) AS CP,
                patternId
            FROM PatternDetail
            WHERE customerFilterKind != 0
            GROUP BY patternId
        ) AS pd2 ON pd2.patternId = p.id
        LEFT JOIN PatternDetail pdCK 
              ON pdCK.customerFilterKind = 1 AND pdCK.patternId = p.id
        LEFT JOIN PatternDetail pdCD 
              ON pdCD.customerFilterKind = 2 AND pdCD.patternId = p.id
        LEFT JOIN PatternDetail pdCP 
              ON pdCP.customerFilterKind = 3 AND pdCP.patternId = p.id
        WHERE 
            (p.customerInclusionKind = 0 
                OR pd.customerId = :customerId
                OR 
                (
                    (pd2.CK = 0 OR pdCK.customerKindId = :customerKindId) AND
                    (pd2.CD = 0 OR pdCD.customerDegreeId = :customerDegreeId) AND
                    (pd2.CP = 0 OR pdCP.customerPishehId = :customerPishehId)
                )
            )
            AND (
                :settlementKind = -1
                OR (:settlementKind = 0 AND p.hasCash = 1)
                OR (:settlementKind = 1 AND p.hasMaturityCash = 1)
                OR (:settlementKind = 2 AND p.hasSanadAndCash = 1)
                OR (:settlementKind = 3 AND p.hasSanad = 1)
                OR (:settlementKind = 4 AND p.hasCredit = 1)
            )
    """
    )
    fun filterCustomerPatterns(
        customerId: Int?,
        customerKindId: Int?,
        customerDegreeId: Int?,
        customerPishehId: Int?,
        settlementKind: Int
    ): List<Int>


    // 2) فیلتر مرکز فروش (Center)
    @Query(
        """
        SELECT DISTINCT p.id
        FROM Pattern p
        LEFT JOIN PatternDetail pd
            ON pd.kind = 3 AND pd.patternId = p.id
        WHERE 
            (p.centerInclusionKind = 0 OR pd.centerId = :centerId)
            AND (
                :settlementKind = -1
                OR (:settlementKind = 0 AND p.hasCash = 1)
                OR (:settlementKind = 1 AND p.hasMaturityCash = 1)
                OR (:settlementKind = 2 AND p.hasSanadAndCash = 1)
                OR (:settlementKind = 3 AND p.hasSanad = 1)
                OR (:settlementKind = 4 AND p.hasCredit = 1)
            )
    """
    )
    fun filterCenterPatterns(
        centerId: Int?,
        settlementKind: Int
    ): List<Int>


    // 3) فیلتر گروه صورتحساب (InvoiceCategory)
    @Query(
        """
        SELECT DISTINCT p.id
        FROM Pattern p
        LEFT JOIN PatternDetail pd
            ON pd.kind = 4 AND pd.patternId = p.id
        WHERE 
            (p.groupInclusionKind = 0 OR pd.invoiceCategoryId = :invoiceCategoryId)
            AND (
                :settlementKind = -1
                OR (:settlementKind = 0 AND p.hasCash = 1)
                OR (:settlementKind = 1 AND p.hasMaturityCash = 1)
                OR (:settlementKind = 2 AND p.hasSanadAndCash = 1)
                OR (:settlementKind = 3 AND p.hasSanad = 1)
                OR (:settlementKind = 4 AND p.hasCredit = 1)
            )
    """
    )
    fun filterInvoiceCategoryPatterns(
        invoiceCategoryId: Int?,
        settlementKind: Int
    ): List<Int>

    // ***** گرفتن Pattern نهایی با بازه‌ی تاریخ
    @Query(
        """
        SELECT *
        FROM Pattern
        WHERE id IN (:ids)
          AND :date BETWEEN fromPersianDate AND toPersianDate
    """
    )
    fun getPatternsFinal(ids: List<Int>, date: String): List<PatternEntity>


    @Query("SELECT * FROM Pattern WHERE id = :id LIMIT 1")
    fun getPatternById(id: Int): LiveData<PatternEntity>

    @Query("SELECT * FROM Pattern WHERE id = :id LIMIT 1")
    suspend fun getPatternByIdSuspend(id: Int): PatternEntity

    @Query("SELECT * FROM Pattern WHERE Id = :id")
    suspend fun getPattern(id: Int): PatternEntity?

    @Query("SELECT * FROM PatternDetail WHERE patternId = :patternId")
    suspend fun getPatternDetailById(patternId: Int): List<PatternDetailEntity>

}
