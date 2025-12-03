package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "factor_table")
data class FactorEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uniqueId: String? = null,
    var formKind: Int? = null,
    var centerId: Int? = null,
    var mainCode: Int? = null,
    var code: Int? = null,
    var createDate: String? = null,
    var persianDate: String? = null,
    var invoiceCategoryId: Int? = null,
    var patternId: Int? = null,
    var dueDate: String? = null,
    var processId: Int? = null,
    var deliveryDate: String? = null,
    var createTime: String? = null,
    var customerId: Int? = null,
    var directionDetailId: Int? = null,
    var visitorId: Int? = null,
    var distributorId: Int? = null,
    var description: String = "",
    var comment: String = "",
    var isCanceled: Int? = null,
    var sabt: Int = 0,
    var hasDetail: Boolean = false,
    var createUserId: Int? = null,
    var saleCenterId: Int? = null,
    var actId: Int? = null,
    var recipientId: Int? = null,
    var settlementKind: Int = -1,
    var createSource: Int = 0,
    var serverSyncDate: Long? = null,
    var finalPrice: Double = 0.0,
    var defaultAnbarId: Int? = null,

    /*

     @TypeConverters(FactorConverters::class)
     @JsonProperty("FactorDetail")
     var factorDetails: List<FactorDetailEntity>? = null,

     @TypeConverters(FactorConverters::class)
     @JsonProperty("FactorDiscount")
     var factorDiscounts: List<FactorDiscountEntity>? = null,

     @TypeConverters(FactorConverters::class)
     @JsonProperty("FactorGiftInfo")
     var factorGiftInfos: List<FactorGiftInfoEntity>? = null,*/

    var hasVAT: Boolean? = null
)
