package com.partsystem.partvisitapp.core.database.dao

import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<GroupProductEntity>)

    @Query("SELECT * FROM GroupProduct WHERE parentId IS NULL AND groupLevel = 1")
    fun getMainGroups(): Flow<List<GroupProductEntity>>

    @Query("SELECT * FROM GroupProduct WHERE parentId = :parentId AND groupLevel = 2")
    fun getSubGroups(parentId: Int): Flow<List<GroupProductEntity>>

    @Query("SELECT * FROM GroupProduct WHERE parentId = :parentId AND groupLevel = 3")
    fun getCategories(parentId: Int): Flow<List<GroupProductEntity>>

    @Query("DELETE FROM GroupProduct")
    suspend fun clearGroupProduct()

    @Query("SELECT COUNT(*) FROM GroupProduct")
    suspend fun getCount(): Int

}

