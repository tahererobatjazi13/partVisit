package com.partsystem.partvisitapp.feature.create_order.model


data class FinalFactorRequestDto(
    val uniqueId: String?,
    val id: Int?,
    val formKind: Int,
    val centerId: Int,
    val createDate: String?,
    val persianDate: String?,
    val invoiceCategoryId: Int?,
    val patternId: Int?,
    val dueDate: String?,
    val deliveryDate: String?,
    val createTime: String?,
    val customerId: Int?,
    val directionDetailId: Int?,
    val visitorId: Int?,
    val distributorId: Int?,
    val description: String?,
    val sabt: Int,
    val createUserId: Int?,
    val saleCenterId: Int?,
    val actId: Int?,
    val recipientId: Int?,
    val settlementKind: Int?,
    val latitude: Double,
    val longitude: Double,
    val defaultAnbarId: Int,
    val factorDetails: List<FinalFactorDetailDto>,
    val factorDiscounts: List<FinalFactorDiscountDto>,
    val factorGiftInfos: List<FinalFactorGiftDto>
)


data class FinalFactorDetailDto(
    val id: Int?,
    val factorId: Int?,
    val sortCode: Int,
    val anbarId: Int,
    val productId: Int?,
    val actId: Int?,
    val unit1Value: Double,
    val unit2Value: Double,
    val price: Double,
    val packingId: Int?,
    val packingValue: Double,
    val vat: Double,
    val productSerial: Int?,
    val isGift: Int,
    val returnCauseId: Int,
    val isCanceled: Int,
    val isModified: Int,
    val description: String,
    val unit1Rate: Double,
    val factorDiscounts: List<FinalFactorDiscountDto>
)

data class FinalFactorDiscountDto(
    val sortCode: Int,
    val discountId: Int,
    val price: Double,
    val discountPercent: Double
)
data class FinalFactorGiftDto(
    val productId: Int,
    val discountId: Int,
    val price: Double,
)


