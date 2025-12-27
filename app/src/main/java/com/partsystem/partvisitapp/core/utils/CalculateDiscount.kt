package com.partsystem.partvisitapp.core.utils

import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.feature.create_order.model.ProductValuesResult
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository

/*
class CalculateDiscount(private val repository: ProductRepository) {

    // این تابع suspend است چون ممکنه به Room وصل شود
    suspend fun fillProductValues(
        anbarId: Int?,
        product: ProductEntity?,
       // currentId: Int?,
        packing: ProductPackingEntity?,
        unit1ValueInput: Double?,
        unit2ValueInput: Double?,
        packingValueInput: Double?,
        isInput: Boolean
    ): ProductValuesResult {
        d("factorViewModelanbarId3", anbarId.toString())
        d("factorViewModelproduct3", product.toString())
        d("factorViewModelpacking3", packing.toString())
        d("factorViewModelunit1ValueInput3", unit1ValueInput.toString())
        d("factorViewModelunit2ValueInput3", unit2ValueInput.toString())
        d("factorViewModelpackingValueInput3", packingValueInput.toString())

        var unit1Value = unit1ValueInput ?: 0.0
        var unit2Value = unit2ValueInput ?: 0.0
        var packingValue = packingValueInput ?: 0.0

        var calculateUnit2Type = 255
        var convertRatio = 0.0
        var hasUnit2 = false

        if (product != null) {
            calculateUnit2Type = product.calculateUnit2Type!!
            convertRatio = product.convertRatio!!
            hasUnit2 = product.unit2Id != null
        }

        val packingUnit1 = packing?.unit1Value ?: 0.0
        val packingUnit2 = packing?.unit2Value ?: 0.0

        // محاسبه unit1/unit2 بر اساس packingValue
        if (packingValue != 0.0) {
            if (packingUnit1 != 0.0) {
                unit1Value = packingUnit1 * packingValue
            } else if (packingUnit2 != 0.0) {
                unit2Value = packingUnit2 * packingValue
            }
        }

        if (hasUnit2) {
            // میانگین
            if (calculateUnit2Type == CalculateUnit2Type.AverageUnits.ordinal &&
                !isInput && anbarId != null && product != null
            ) {
                val mojoodi = repository.getMojoodi(anbarId, product.id)
                val unit1Amount = mojoodi?.remainValue ?: 1.0
                val unit2Amount = mojoodi?.remainValue2 ?: 1.0

                if (unit1Value != 0.0 && unit1Amount != 0.0) {
                    unit2Value = unit2Amount * unit1Value / unit1Amount
                } else if (unit2Value != 0.0 && unit2Amount != 0.0) {
                    unit1Value = unit1Amount * unit2Value / unit2Amount
                }
            }
            // فرمول استاندارد
            else if (calculateUnit2Type == CalculateUnit2Type.StandardFormula.ordinal) {
                if (unit1Value != 0.0 && convertRatio != 0.0) {
                    unit2Value = unit1Value * convertRatio
                } else if (unit2Value != 0.0 && convertRatio != 0.0) {
                    unit1Value = unit2Value / convertRatio
                }
            }
        }

        // محاسبه packingValue اگر صفر باشد
        if (packingValue == 0.0) {
            if (packingUnit1 != 0.0 && unit1Value != 0.0) {
                packingValue = unit1Value / packingUnit1
            } else if (packingUnit2 != 0.0 && unit2Value != 0.0) {
                packingValue = unit2Value / packingUnit2
            }
        }

        return ProductValuesResult(
            unit1Value = unit1Value,
            unit2Value = unit2Value,
            packingValue = packingValue
        )
        d("factorViewModelunit1Value4", unit1Value.toString())
        d("factorViewModelunit2Value4", unit2Value.toString())
        d("factorViewModelpackinggValue4", packingValue.toString())

    }

}
*/


class CalculateDiscount(private val repository: ProductRepository) {



/*
        suspend fun calculateProductDiscount(detail: FactorDetailEntity, factorDiscounts: List<FactorDiscountEntity>) {
            var totalDiscount = 0.0
            var totalAddition = 0.0

            // 1. محاسبه تخفیف‌ها و افزودنی‌ها
            for (fd in factorDiscounts) {
                if (fd.factorDetailId == detail.productId) { // یا detail.id اگر id داشته باشی
                    val discount = discountRepository.getDiscount(fd.discountId)
                    if (discount != null) {
                        when (discount.kind) {DiscountKind.Discount.ordinal -> {
                                totalDiscount += fd.price
                            }DiscountKind.Addition.ordinal -> {
                                totalAddition += fd.price
                            }
                        }
                    }
                }
            }

            detail.totalDiscountPrice = totalDiscount
            detail.totalAdditionalPrice = totalAddition

            // 2. گرفتن محصول برای درصد مالیات و عوارض
            val product = detail.product?.product
                ?: repository.getProduct(detail.productId, detail.actId)

            // 3. محاسبه مالیات و عوارض (اگر فاکتور شامل آن باشد)
            if (hasVatAndToll(detail)) {
                val priceAfterDiscount = detail.getPriceAfterDiscount()
                detail.toll = Math.round(product.tollPercent * priceAfterDiscount).toDouble()
                detail.vat = Math.round(product.vatPercent * priceAfterDiscount).toDouble()
            }
        }
*/

        private suspend fun hasVatAndToll(detail: FactorDetailEntity): Boolean {
            // مثلاً از FactorHeader چک کن:
            // return factorHeaderRepository.get(detail.factorId)?.hasVat == true
            // برای سادگی فعلاً true فرض می‌کنیم
            return true
        }


    suspend fun fillProductValues(
        anbarId: Int?,
        product: ProductEntity?,
        packing: ProductPackingEntity?,
        unit1ValueInput: Double?,
        unit2ValueInput: Double?,
        packingValueInput: Double?,
        isInput: Boolean
    ): ProductValuesResult {
        var unit1Value = unit1ValueInput ?: 0.0
        var unit2Value = unit2ValueInput ?: 0.0
        var packingValue = packingValueInput ?: 0.0

        var calculateUnit2Type = 255
        var convertRatio = 0.0
        var hasUnit2 = false

        if (product != null) {
            calculateUnit2Type = product.calculateUnit2Type!!
            convertRatio = product.convertRatio!!
            hasUnit2 = product.unit2Id != null
        }

        val packingUnit1 = packing?.unit1Value ?: 0.0
        val packingUnit2 = packing?.unit2Value ?: 0.0

        // مرحله ۱: اگر packingValue ورودی داده شده، unit1/unit2 را از آن محاسبه کن
        if (isInput && packingValueInput != null && packingValue != 0.0) {
            if (packingUnit1 != 0.0) {
                unit1Value = packingUnit1 * packingValue
                unit2Value = 0.0 // اجازه نده unit2 تداخل ایجاد کند
            } else if (packingUnit2 != 0.0) {
                unit2Value = packingUnit2 * packingValue
                unit1Value = 0.0
            }
        }

        // مرحله ۲: محاسبه unit2 بر اساس نوع محاسبه
        if (hasUnit2) {
            if (calculateUnit2Type == CalculateUnit2Type.AverageUnits.ordinal &&
                !isInput && anbarId != null && product != null
            ) {
                val mojoodi = repository.getMojoodi(anbarId, product.id)
                val unit1Amount = mojoodi?.remainValue ?: 1.0
                val unit2Amount = mojoodi?.remainValue2 ?: 1.0

                if (unit1Value != 0.0 && unit1Amount != 0.0) {
                    unit2Value = unit2Amount * unit1Value / unit1Amount
                } else if (unit2Value != 0.0 && unit2Amount != 0.0) {
                    unit1Value = unit1Amount * unit2Value / unit2Amount
                }
            } else if (calculateUnit2Type == CalculateUnit2Type.StandardFormula.ordinal) {
                if (unit1Value != 0.0 && convertRatio != 0.0) {
                    unit2Value = unit1Value * convertRatio
                } else if (unit2Value != 0.0 && convertRatio != 0.0) {
                    unit1Value = unit2Value / convertRatio
                }
            }
        }

        // مرحله ۳: اگر packingValue صفر است، آن را از unit1/unit2 محاسبه کن (فقط وقتی isInput نباشد!)
        if (!isInput || packingValue == 0.0) {
            if (packingUnit1 != 0.0 && unit1Value != 0.0) {
                packingValue = unit1Value / packingUnit1
            } else if (packingUnit2 != 0.0 && unit2Value != 0.0) {
                packingValue = unit2Value / packingUnit2
            }
        }

        return ProductValuesResult(
            unit1Value = unit1Value,
            unit2Value = unit2Value,
            packingValue = packingValue
        )
    }
}