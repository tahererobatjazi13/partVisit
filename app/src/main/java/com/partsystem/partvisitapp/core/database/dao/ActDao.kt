package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActDao {

    // INSERT / REPLACE Operations
    /**
     * درج لیست ActEntity با استراتژی REPLACE
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActs(list: List<ActEntity>)

    /**
     * درج لیست ActDetailEntity با استراتژی REPLACE
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActDetails(list: List<ActDetailEntity>)

    // ─────────────────────────────────────────────────────
    // DELETE Operations
    @Query("DELETE FROM Act")
    suspend fun clearAct()

    @Query("DELETE FROM ActDetail")
    suspend fun clearActDetails()

    // ─────────────────────────────────────────────────────
    // SELECT Operations
    /**
     * دریافت تمام Acts به صورت Flow با مرتب‌سازی صعودی بر اساس Code
     */
    @Query("SELECT * FROM Act ORDER BY Code ASC")
    fun getActs(): Flow<List<ActEntity>>

    /**
     * دریافت Acts مرتبط با یک Pattern خاص و نوع مشخص
     */
    @Query(
        """
        SELECT a.* 
        FROM Act AS a
        INNER JOIN PatternDetail AS pd ON pd.actId = a.id
        WHERE pd.patternId = :patternId
          AND a.kind = :kind
        ORDER BY pd.isDefault DESC, a.code ASC
    """
    )
    suspend fun getActsByPatternId(patternId: Int, kind: Int): List<ActEntity>

    /**
     *  دریافت یک Act بر اساس آی دی
     */
    @Query("SELECT * FROM Act WHERE id = :actId LIMIT 1")
    suspend fun getActById(actId: Int): ActEntity?

    /**
     * دریافت شناسه Act پیش‌فرض برای یک Pattern و Kind مشخص
     */
    @Query(
        """
        SELECT pd.actId
        FROM PatternDetail pd
        INNER JOIN Act a 
            ON a.id = pd.actId 
            AND pd.isDefault = 1
        WHERE pd.patternId = :patternId
          AND a.kind = :kind
        LIMIT 1
        """
    )
    suspend fun getPatternDetailActId(
        patternId: Int,
        kind: Int
    ): Int?

    /**
     * دریافت جزئیات Act برای ترکیب خاص actId + productId
     */
    @Query(
        """
        SELECT * FROM ActDetail
        WHERE actId = :actId
        AND productId = :productId
        LIMIT 1
    """
    )
    suspend fun getActDetail(
        actId: Int,
        productId: Int
    ): ActDetailEntity?
}
