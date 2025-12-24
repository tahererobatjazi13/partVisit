package com.partsystem.partvisitapp.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.network.modelDto.FactorDetailOfflineModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FactorDao {


    @Update
    fun updateFactor(factor: FactorHeaderEntity)

    @Delete
    fun deleteFactor(factor: FactorHeaderEntity)

    @Query("SELECT * FROM factor_header_table WHERE id = :id")
    fun getFactorById(id: Int): FactorHeaderEntity?

    @Query("SELECT * FROM factor_header_table")
    suspend fun getAllFactors(): List<FactorHeaderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(details: List<FactorDetailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(detail: FactorDetailEntity)

    @Update
    suspend fun updateFactorDetail(item: FactorDetailEntity)

    @Query("SELECT * FROM factor_detail_table")
    fun getAllFactorDetail(): LiveData<List<FactorDetailEntity>>

    @Query("DELETE FROM factor_detail_table WHERE productId = :productId")
    suspend fun deleteFactorDetail(productId: Int)

    @Query("DELETE FROM factor_header_table")
    suspend fun clearFactorHeader()

    @Query("DELETE FROM factor_detail_table")
    suspend fun clearFactorDetails()

    @Query("DELETE FROM factor_discount_table")
    suspend fun clearFactorDiscount()

    @Query("DELETE FROM factor_gift_info_table")
    suspend fun clearFactorGiftInfo()


////////////////

    // Header

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorHeader(header: FactorHeaderEntity): Long

    @Query("SELECT * FROM factor_header_table WHERE id = :id LIMIT 1")
    fun getHeaderById(id: Int): LiveData<FactorHeaderEntity>

    @Update
    suspend fun updateFactorHeader(header: FactorHeaderEntity)

    @Update
    suspend fun updateHeader(header: FactorHeaderEntity)

    @Query("SELECT * FROM factor_header_table WHERE uniqueId = :uniqueId LIMIT 1")
    suspend fun getHeaderByUniqueId(uniqueId: String): FactorHeaderEntity?

    @Query("SELECT * FROM factor_header_table WHERE id = :localId LIMIT 1")

    suspend fun getHeaderByLocalId(localId: Long): FactorHeaderEntity?

    @Query("SELECT * FROM factor_header_table ORDER BY id DESC ")
    fun getAllHeaders(): Flow<List<FactorHeaderEntity>>

    @Query("DELETE FROM factor_header_table WHERE uniqueId = :uniqueId")
    suspend fun deleteHeaderByUniqueId(uniqueId: String)


    // Detail


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactorDetail(detail: FactorDetailEntity): Long


    @Delete
    suspend fun deleteDetail(detail: FactorDetailEntity)

    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId")
    suspend fun getDetailsForHeader(factorId: String): List<FactorDetailEntity>


    @Query(
        """
    SELECT COUNT(*) FROM factor_detail_table 
    WHERE factorId = :factorId
    """
    )
    fun getFactorItemCount(factorId: Int): LiveData<Int>


    @Query("SELECT * FROM factor_detail_table WHERE factorId = :factorId")
    fun getFactorDetails(factorId: Int): Flow<List<FactorDetailEntity>>

    @Upsert
    suspend fun upsert(detail: FactorDetailEntity)

    @Query(
        """
    DELETE FROM factor_detail_table
    WHERE factorId = :factorId AND productId = :productId
    """
    )
    suspend fun deleteByFactorAndProduct(
        factorId: Int,
        productId: Int
    )

    @Query(
        """
    DELETE FROM factor_detail_table 
    WHERE factorId = :factorId
    """
    )
    suspend fun clearFactor(factorId: Int)


    @Query(
        """
        SELECT 
            fd.factorId,
            fd.productId,
            p.name AS productName,
            p.unitName AS unit1Name,
            pp.packingName AS packingName,
            fd.unit1Value,
            fd.unit2Value,
            fd.packingValue,
            fd.unit1Rate,
            fd.vat
        FROM factor_detail_table fd
        LEFT JOIN product_table p ON fd.productId = p.id
        LEFT JOIN product_packing_table pp ON fd.packingId = pp.id
        WHERE fd.factorId = :factorId
        ORDER BY fd.sortCode
    """
    )
    fun getFactorDetailUi(factorId: Int): Flow<List<FactorDetailOfflineModel>>

    // Discounts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: FactorDiscountEntity): Long

    /*  @Query("SELECT * FROM factor_discount_table WHERE factorId = :factorId")
      suspend fun getDiscountsForHeader(factorId: String): List<FactorDiscountEntity>

      @Query("DELETE FROM factor_discount_table WHERE factorId = :factorId")
      suspend fun deleteDiscountsForHeader(factorId: String)*/

    // Gift


    @Insert
    suspend fun insertFactorGift(gift: FactorGiftInfoEntity): Long

    @Query("SELECT * FROM factor_gift_info_table WHERE factorId = :factorId")
    suspend fun getFactorGifts(factorId: Int): List<FactorGiftInfoEntity>

    @Query("DELETE FROM factor_gift_info_table WHERE id = :headerId")
    suspend fun deleteHeader(headerId: Long)
}
