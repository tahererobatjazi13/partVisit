package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity

@Dao
interface ApplicationSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<ApplicationSettingEntity>)

    @Query("SELECT * FROM application_setting_table")
    suspend fun getAll(): List<ApplicationSettingEntity>

    @Query("DELETE FROM application_setting_table")
    suspend fun clearAll()
}
