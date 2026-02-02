package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "FactorHeader")
data class FactorHeaderEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uniqueId: String? = null,
    var formKind: Int? = null,
    var centerId: Int? = null,
    var createDate: String? = null,
    var persianDate: String? = null,
    var invoiceCategoryId: Int? = null,
    var patternId: Int? = null,
    var dueDate: String? = null,
    var deliveryDate: String? = null,
    var createTime: String? = null,
    var customerId: Int? = null,
    var directionDetailId: Int? = null,
    var visitorId: Int? = null,
    var distributorId: Int? = null,
    var description: String = "",
    var sabt: Int = 0,
    var hasDetail: Boolean = false,
    var createUserId: Int? = null,
    var saleCenterId: Int? = null,
    var actId: Int? = null,
    var recipientId: Int? = null,
    var settlementKind: Int = -1,
    var createSource: Int = 0,
    var finalPrice: Double = 0.0,
    var defaultAnbarId: Int? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    val productSelectionType: String= "" // "catalog" یا "group"

)

/*
[{
    "uniqueId": "F6EA69AA-020E-471D-A767-3CB93F90062A",
    "id": 10,
    "formKind":17,
    "centerId": 1,
    "createDate": "2025-12-20",
    "persianDate": "1404/09/29",
    "invoiceCategoryId": 10,
    "patternId": 71,
    "dueDate": "2025-12-20",
    "persianDueDate": "1404/09/29",
    "customerId": 3908,
    "visitorId": 2471,
    "distributorId": null,
    "description": "ثبت از اندروید",
    "sabt": 1,
    "createUserId": 1,
    //"sabtPayment": true,

    "saleCenterId": 3,
    "actId":195,
    "settlementKind": 0,

    "createTime": "11:22:57",
    "directionDetailId": 23261,
    "latitude": 0,
    "longitude": 0,
    "factorDetails": [
    {
        "factorId": 10,
        "id": 1,
        "sortCode": 1,
        "anbarId": 6,
        "productId": 107,
        "actId": 195,
        "unit1Value": 20,
        "unit2Value": 0,
        "price": 4000000.00,
        "packingId": 3,
        "packingValue": 1,
        "vat": 400000.00,
        "productSerial": null,
        "isGift": 0,
        "returnCauseId": 0,
        "isCanceled": 0,
        "isModified": 0,
        "unit1Rate": 200000,

    },
    {
        "factorId": 10,
        "id": 2,
        "sortCode": 2,
        "anbarId": 6,
        "productId": 7,
        "actId": 195,
        "unit1Value": 5,
        "unit2Value": 0,
        "price": 21130000.00,
        "packingId": 49,
        "packingValue": 1.25,
        "vat": 0,
        "productSerial": null,
        "isGift": 0,
        "unit1Rate": 4226000,
        "factorDiscounts": [
        {
            "sortCode": 1,
            "discountId": 5,
            "price": 1690400.00,
            "discountPercent": 0
        }
        ]
    }
    ,{
    "factorId": 10,
    "id": 3,
    "sortCode": 3,
    "anbarId": 6,
    "productId": 107,
    "actId": 203,
    "unit1Value": 1,
    "unit2Value": 0,
    "price": 200000.00,
    "packingId": 3,
    "packingValue":0.05,
    "vat": 0,
    "productSerial": null,
    "isGift": 1,
    "unit1Rate": 200000.00,
    "factorDiscounts": [
    {
        "sortCode": 1,
        "discountId": 107,
        "price":200000.00,
        "discountPercent": 0
    }
    ]
}
    ],
    "factorGiftInfos": [
    {
        "discountId": 107,
        "productId": 107,
        "price": 200000.00,
    }
    ]
}
]*/
