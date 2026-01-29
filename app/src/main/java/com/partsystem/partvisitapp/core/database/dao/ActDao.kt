package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ActWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ActDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActs(list: List<ActEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActDetails(list: List<ActDetailEntity>)

    @Query("SELECT * FROM Act ORDER BY Code ASC")
    fun getActs(): Flow<List<ActEntity>>

    @Transaction
    @Query("SELECT * FROM Act")
    suspend fun getActWithDetails(): List<ActWithDetails>
    

    @Query("DELETE FROM Act")
    suspend fun clearAct()

    @Query("DELETE FROM ActDetail")
    suspend fun clearActDetails()

    @Query("""
        SELECT a.* 
        FROM Act AS a
        INNER JOIN PatternDetail AS pd ON pd.actId = a.id
        WHERE pd.patternId = :patternId
          AND a.kind = :kind
        ORDER BY pd.isDefault DESC, a.code ASC
    """)
    suspend fun getActsByPatternId(patternId: Int, kind: Int): List<ActEntity>

    @Query("SELECT * FROM Act WHERE id = :actId LIMIT 1")
    suspend fun getActById(actId: Int): ActEntity?

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


    @Query("""
        SELECT * FROM ActDetail
        WHERE actId = :actId
        AND productId = :productId
        LIMIT 1
    """)
    suspend fun getActDetail(
        actId: Int,
        productId: Int
    ): ActDetailEntity?
}
