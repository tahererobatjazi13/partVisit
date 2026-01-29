package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleDetailEntity
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleEntity

@Dao
interface VisitScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitSchedule(list: List<VisitScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitScheduleDetails(details: List<VisitScheduleDetailEntity>)

    @Transaction
    @Query("SELECT * FROM VisitSchedule WHERE visitorId = :visitorId")
    suspend fun getScheduleByVisitor(visitorId: Int): List<VisitScheduleEntity>

    @Query("DELETE FROM VisitSchedule")
    suspend fun clearVisitSchedule()

    @Query("DELETE FROM VisitScheduleDetail")
    suspend fun clearVisitScheduleDetails()
}
