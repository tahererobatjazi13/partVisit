package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Discount")
data class DiscountEntity(
    @PrimaryKey
    val id: Int,
    val formType: Int,
    val code: Int,
    val name: String,
    val kind: Int,
    val isAutoCalculate: Boolean,
    val applyKind: Int,
    val calculationKind: Int,
    val inclusionKind: Int,
    val paymentKind: Int,
    val priceAmount: Double,
    val priceKind: Int,
    val sabt: Boolean,
    val beginDate: String,
    val persianBeginDate: String,
    val hasCash: Boolean,
    val hasMaturityCash: Boolean,
    val hasSanad: Boolean,
    val hasSanadAndCash: Boolean,
    val hasCredit: Boolean,
    val dayCount: Int,
    val isSystem: Boolean,
    val hasUseToolsPercnet: Boolean,
    val hasUseToolsPrice: Boolean?,
    val unitKind: Int?,
    val hasLastControl: Boolean,
    val executeKind: Int,
    val maxPrice: Double,
    val customerFilterKind: Int,
    val toDate: String?,
    val toPersianDate: String?,
    ) {
    val productIds: List<Int>
        get() = discountProducts?.map { it.productId } ?: emptyList()

    fun getKindName(): String = when (kind) {
        0.toByte().toInt() -> "تخفیف"
        1.toByte().toInt() -> "اضافه"
        2.toByte().toInt() -> "کسورات"
        else -> ""
    }

    @Ignore
    var discountEshantyuns: List<DiscountEshantyunsEntity>? = null
    @Ignore
    var discountGifts: List<DiscountGiftsEntity>? = null
    @Ignore
    var discountGroups: List<DiscountGroupsEntity>? = null
    @Ignore
    var discountProducts: List<DiscountProductsEntity>? = null
    @Ignore
    var discountProductKinds: List<DiscountProductKindsEntity>? = null
    @Ignore
    var discountProductKindInclusions: List<DiscountProductKindInclusionsEntity>? = null
    @Ignore
    var discountStairs: List<DiscountStairsEntity>? = null
    @Ignore
    var discountUsers: List<DiscountUsersEntity>? = null
}