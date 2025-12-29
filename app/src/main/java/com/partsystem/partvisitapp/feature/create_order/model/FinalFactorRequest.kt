package com.partsystem.partvisitapp.feature.create_order.model


data class FinalFactorRequestDto(
    val uniqueId: String?,
    val id: Int?,
    val formKind: Int,
    val centerId: Int,
    val code: Int?,
    val createDate: String?,
    val invoiceCategoryId: Int?,
    val patternId: Int?,
    val dueDate: String?,
    val customerId: Int?,
    val visitorId: Int?,
    val description: String?,
    val sabt: Int,
    val createUserId: Int?,
    val saleCenterId: Int?,
    val actId: Int?,
    val settlementKind: Int?,
    val deliveryDate: String?,
    val createTime: String?,
    val directionDetailId: Int?,
    val latitude: Int,
    val longitude: Int,
    val factorDetails: List<FinalFactorDetailDto>,
    val factorGiftInfos: List<FinalFactorGiftDto>
)


data class FinalFactorDetailDto(
    val factorId: Int?,
    val id: Int?,
    val sortCode: Int,
    val anbarId: Int,
    val productId: Int?,
    val actId: Int?,
    val unit1Value: Int,
    val unit2Value: Int,
    val price: Int,
    val description: String?,
    val packingId: Int?,
    val packingValue: Double,
    val vat: Int,
    val productSerial: Int,
    val isGift: Int,
    val returnCauseId: Int,
    val isCanceled: Int,
    val isModified: Int,
    val unit1Rate: Int,
    val factorDiscounts: List<FinalFactorDiscountDto>
)

data class FinalFactorDiscountDto(
    val id: Int?,
    val sortCode: Int,
    val discountId: Int?,
    val price: Int,
    val arzPrice: Int,
    val factorDetailId: Int?,
    val discountPercent: Int
)

data class FinalFactorGiftDto(
    val id: Int?,
    val factorId: Int?,
    val discountId: Int?,
    val productId: Int?,
    val price: Int,
    val arzPrice: Int
)


