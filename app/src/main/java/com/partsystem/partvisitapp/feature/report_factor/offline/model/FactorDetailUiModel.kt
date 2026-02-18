package com.partsystem.partvisitapp.feature.report_factor.offline.model

data class FactorDetailUiModel(
    val id: Int,
    val factorId: Int,
    val productId: Int,
    val productName: String?,
    val packingId: Int?,
    val packingName: String?,
    val packingValue: Double,
    val unitPerPack: Double,
    val unit1Name: String?,
    val unit1Value: Double,
    val unit2Value: Double,
    val unit1Rate: Double,
    val vat: Double,
    val isGift: Int,
    val discountPrice: Double,
    val sortCode: Int? = null
)

//
//data class FactorDetailUiModel(
//    val product: ProductEntity,
//    val packings: List<ProductPackingEntity>,
//    val factorId: Int,
//    val productName: String?,
//    val packingName: String?,
//    val packingValue: Double,
//    val unitPerPack: Double,
//    val unit1Name: String?,
//    val unit1Value: Double,
//    val unit2Value: Double,
//    val unit1Rate: Double,
//    val vat: Double,
//    val isGift: Int,
//    val discountPrice: Int,
//    val rate: Double,
//    val vatPercent: Float,
//    val tollPercent: Float,
//    val finalRate: Double
//)*/


fun FactorDetailUiModel.getPackingValueFormatted(): String {
    // در حالت بدون بسته‌بندی (unitPerPack <= 0) مقدار unit1Value را با :0 نمایش بده
    if (unitPerPack <= 0) {
        val valueText = if (unit1Value % 1 == 0.0) {
            unit1Value.toInt().toString()
        } else {
            unit1Value.toString()
        }
        return "$valueText:0"
    }

    // محاسبه مقدار برای بسته‌بندی معتبر
    val fullPacks = kotlin.math.floor(unit1Value / unitPerPack).toInt()
    val remain = unit1Value % unitPerPack

    // فرمت‌دهی باقیمانده
    val remainText = if (remain % 1 == 0.0) {
        remain.toInt().toString()
    } else {
        remain.toString()
    }

    return "$remainText:$fullPacks"
}
