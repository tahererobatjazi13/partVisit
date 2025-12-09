package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "factor_detail_table")
data class FactorDetailEntity(
    @PrimaryKey(autoGenerate = true)
    var factorId: Int? = null,
    var id: Int? = null,
    var sortCode: Int? = null,
    var anbarId: Int? = null,
    var productId: Int? = null,
    var actId: Int? = null,
    var unit1Value: Double? = 0.0,
    var unit2Value: Double? = 0.0,
    var price: Double? = 0.0,
    var description: String? = null,
    var packingId: Int? = null,
    var packingValue: Double? = 0.0,
    var vat: Double? = 0.0,
    var productSerial: Int? = null,
    var isGift: Int? = 0,
    var returnCauseId: Int? = 0,
    var isCanceled: Int? = 0,
    var isModified: Int? = 0,
    var unit1Rate: Double? = 0.0,

    // var factorDiscounts: MutableList<FactorDiscountEntity> = mutableListOf()
) {

    @Transient
    var product: ProductEntity? = null

    fun setProduct(p: ProductEntity) {
        this.product = p
        this.productId = p.id
    }

    fun getProduct(): ProductEntity? = product


    @Transient
    var packing: ProductPackingEntity? = null

    fun setPacking(value: ProductPackingEntity?) {
        if (value == null) {
            packing = null
            packingId = null
            packingCode = null
            packingName = ""
            packingValue = 0.0
            return
        }

        // اگر تازه تنظیم می‌شود یا پَک قبلی متفاوت است
        if (packing == null || packing?.id != value.id) {
            packing = value

            packingId = value.packingId
            packingCode = value.packingCode
            packingName = value.packingName

            // محاسبه unit و packingValue
            val values = CalculateDiscount.getInstance(null)
                .fillProductValues(
                    anbarId,
                    product,
                    id,
                    value,
                    unit1Value,
                    null,
                    null,
                    false
                )

            unit2Value = values["Unit2Value"] as? Double ?: unit2Value
            packingValue = values["PackingValue"] as? Double ?: packingValue
        }
    }

    fun getPacking(): ProductPackingEntity? {
        if (packing == null && packingId != null) {
            val list = product?.packings ?: return null
            packing = list.find { it.packingId == packingId }
        }
        return packing
    }

}
