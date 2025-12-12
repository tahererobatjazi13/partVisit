package com.partsystem.partvisitapp.core.utils

import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductFullData
import com.partsystem.partvisitapp.core.network.modelDto.ProductValuesResult
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository

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
    }

}
