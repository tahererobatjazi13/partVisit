package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.MojoodiEntity

@Dao
interface MojoodiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMojoodi(mojoodi: List<MojoodiEntity>)

    @Query("SELECT * FROM mojoodi_table WHERE anbarId = :anbarId AND productId = :productId LIMIT 1")
    suspend fun getMojoodi(anbarId: Int, productId: Int): MojoodiEntity?

    @Query("DELETE FROM mojoodi_table")
    suspend fun clearMojoodi()
}
