package com.partsystem.partvisitapp.feature.main.home.model

data class DiscountEshantyunsDto(
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val saleUnitKind: Int,
    val fromValue: Double,
    val toValue: Double,
    val anbarId: Int,
    val productId: Int,
    val unitKind: Int,
    val value: Double,
    val packingId: Int,
    val ratio: Double
)

data class DiscountGiftsDto(
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val fromPrice: Double,
    val toPrice: Double,
    val anbarId: Int,
    val productId: Int,
    val unitKind: Int,
    val value: Double,
    val packingId: Int,
    val ratio: Int,
)

data class DiscountGroupsDto(
    val id: Int,
    val discountId: Int,
    val kind: Int,
    val groupId: Int,
    val groupDetailId: Int,
    val rastehId: Int
)

data class DiscountProductKindInclusionsDto(
    val id: Int,
    val discountId: Int,
    val productKindId: Int,
)

data class DiscountProductKindsDto(
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val fromProductKind: Int,
    val toProductKind: Int,
    val discountPercent: Double,
    val minPrice: Double,
)

data class DiscountProductsDto(
    val id: Int,
    val discountId: Int,
    val kind: Int,
    val productId: Int
)

data class DiscountStairsDto(
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val fromPrice: Double,
    val toPrice: Double,
    val price: Double,
    val unitKind: Int,
    val ratio: Double
)

data class DiscountUsersDto(
    val id: Int,
    val discountId: Int,
    val userId: Int
)

data class DiscountCustomersDto(
    val id: Int,
    val discountId: Int,
    val customerId: Int,
    val customerKindId: Int,
    val customerDegreeId: Int,
    val customerPishehId: Int,
    val tafsiliGroupId: Int,
    val tafsiliGroupDetailId: Int,
    val customerPriceAmount: Double,
    val maxCustomerPriceAmount: Double
)