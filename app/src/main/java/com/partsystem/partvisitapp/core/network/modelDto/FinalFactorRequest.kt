package com.partsystem.partvisitapp.core.network.modelDto

data class FinalFactorRequest(
    val uniqueId: String?,
    val id: Int?,
    val formKind: Int?,
    val centerId: Int?,
    val mainCode: Int?,
    val code: Int?,
    val createDate: String?,
    val invoiceCategoryId: Int?,
    val patternId: Int?,
    val dueDate: String?,
    val customerId: Int?,
    val visitorId: Int?,
    val description: String?,
    val sabt: Int?,
    val createUserId: Int?,
    val saleCenterId: Int?,
    val actId: Int?,
    val settlementKind: Int?,
    val deliveryDate: String?,
    val createTime: String?,
    val directionDetailId: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val factorDetails: List<FactorDetail>,
    val factorGiftInfos: List<FactorGiftInfo>
)

data class FactorDetail(
    val factorId: Int?,
    val id: Int?,
    val sortCode: Int?,
    val anbarId: Int?,
    val productId: Int?,
    val actId: Int?,
    val unit1Value: Double?,
    val unit2Value: Double?,
    val price: Double?,
    val description: String?,
    val packingId: Int?,
    val packingValue: Double?,
    val vat: Double?,
    val productSerial: Int?,
    val isGift: Int?,
    val returnCauseId: Int?,
    val isCanceled: Int?,
    val isModified: Int?,
    val unit1Rate: Double?,
    val factorDiscounts: List<FactorDiscount>
)

data class FactorDiscount(
    val id: Int?,
    val sortCode: Int?,
    val discountId: Int?,
    val price: Double?,
    val arzPrice: Double?,
    val factorDetailId: Int?,
    val discountPercent: Double?
)

data class FactorGiftInfo(
    val id: Int?,
    val factorId: Int?,
    val discountId: Int?,
    val productId: Int?,
    val price: Double?,
    val arzPrice: Double?
)
