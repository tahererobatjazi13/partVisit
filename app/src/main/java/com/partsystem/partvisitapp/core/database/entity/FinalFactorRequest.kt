package com.partsystem.partvisitapp.core.database.entity

data class FinalFactorRequest(
    var uniqueId: String?,
    var id: Int?,
    var formKind: Int?,
    var centerId: Int?,
    var code: Int?,
    var createDate: String?,
    var patternId: Int?,
    var dueDate: String?,
    var saleCenterId: Int?,
    var customerId: Int?,
    var visitorId: Int?,
    var description: String?,
    var sabt: Int?,
    var createUserId: Int?,
    var actId: Int?,
    var settlementKind: Int?,
    var deliveryDate: String?,
    var createTime: String?,
    var directionDetailId: Int?,
    var latitude: Double?,
    var longitude: Double?,

    var factorDetails: List<FactorDetailEntity>,
    var factorGiftInfos: List<FactorGiftInfoEntity>
)
