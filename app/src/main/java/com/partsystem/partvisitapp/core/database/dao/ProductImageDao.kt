package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity

@Dao
interface ProductImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ProductImageEntity>)

    @Query("SELECT * FROM product_images_table WHERE ownerId = :ownerId")
    fun getImagesByProductId(ownerId: Int): LiveData<List<ProductImageEntity>>

    @Query("DELETE FROM product_images_table")
    suspend fun clearProductImage()

    @Query("SELECT * FROM product_images_table")
    suspend fun getAllImagesOnce(): List<ProductImageEntity>

}

