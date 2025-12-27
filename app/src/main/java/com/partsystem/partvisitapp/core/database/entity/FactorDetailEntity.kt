package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.CalculateDiscount
import com.partsystem.partvisitapp.core.utils.formatFloat
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import kotlin.math.floor

/*
@Entity(
    tableName = "factor_detail_table",
    primaryKeys = ["factorId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = FactorHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FactorDetailEntity(
    val factorId: Int,
    var productId: Int,
    var sortCode: Int? = null,
    var anbarId: Int? = null,
    var actId: Int? = null,
    var unit1Value: Double = 0.0,
    var unit2Value: Double = 0.0,
    var price: Double = 0.0,
    var description: String? = null,
    var packingId: Int? = null,
    var packingValue: Double = 0.0,
    var vat: Double = 0.0,
    var productSerial: Int? = null,
    var isGift: Int = 0,
    var returnCauseId: Int = 0,
    var isCanceled: Int = 0,
    var isModified: Int = 0,
    var unit1Rate: Double = 0.0
    // var factorDiscounts: MutableList<FactorDiscountEntity> = mutableListOf()
) {
    @Transient
    var packing: ProductPackingEntity? = null

    @Ignore
    var product: ProductWithPacking? = null

    @Ignore
    fun resolvePacking(): ProductPackingEntity? {
        if (packing == null && packingId != null) {
            packing = product?.packings
                ?.firstOrNull { it.packingId == packingId }
        }
        return packing
    }
    *//*
        @Ignore
        fun getPackingValueFormatted(): String {

            if (packingValue <= 0 || unit1Value <= 0) return ""

            val resolvedPacking = resolvePacking() ?: return ""

            val remain = unit1Value % resolvedPacking.unit1Value
            return "${floor(packingValue)} : ${formatFloat(remain)}"
        }*//*


    @Ignore
    fun getPackingValueFormatted(): String {
        val packing = resolvePacking() ?: return ""
        if (packingValue > 0) {

            val remain = unit1Value % packing.unit1Value
            return "${formatFloat(floor(packingValue))} : ${formatFloat(remain)}"
            Log.d("productdetailgetremainunit1Value", unit1Value.toString())
            Log.d("productdetailgetremainunit1Value", packing.unit1Value.toString())
            Log.d("productdetailgetremain", remain.toString())
        }

        return ""
    }

    @Ignore
    @Transient
    var repository: ProductRepository? = null

    var anbarCode: String? = null
    var anbarName: String? = null
    var productCode: String? = null
    var productName: String? = null
    var packingCode: Int? = null
    var packingName: String = ""


    @Ignore
    fun applyProduct(product: ProductWithPacking) {
        this.product = product

        this.productId = product.product.id
        this.productCode = product.product.code
        this.productName = product.product.name

        this.packing = null

        val defaultPack = product.packings.firstOrNull { it.isDefault }
        if (defaultPack != null) {
            applyPacking(defaultPack)
        }
    }

    // ست کردن Packing
    fun applyPacking(value: ProductPackingEntity?) {
        if (value == null) {
            packing = null
            packingId = null
            packingCode = null
            packingName = ""
            packingValue = 0.0
            return
        }

        if (packing == null || packing?.id != value.id) {
            packing = value
            packingId = value.packingId
            packingCode = value.packingCode
            packingName = value.packingName ?: ""

            // اگر Repository تنظیم نشده بود، کاری انجام نده
            val repo = repository ?: return

            // چون fillProductValues suspend است، باید Coroutine اجرا شود
            CoroutineScope(Dispatchers.Main).launch {
                product?.let { prod ->
                    val calculator = CalculateDiscount(repo)
                    val values = calculator.fillProductValues(
                        anbarId = anbarId,
                        product = prod.product,
                        // currentId = id,
                        packing = packing,
                        unit1ValueInput = unit1Value,
                        unit2ValueInput = null,
                        packingValueInput = null,
                        isInput = false
                    )
                    // ست کردن مقادیر خروجی
                    unit2Value = values.unit2Value
                    packingValue = values.packingValue
                }
            }
        }
    }

    @Ignore
    // @JsonIgnore
    fun setUnit1Value1(value: Double, anbarId: Int) {
        unit1Value = value

        val packing = packing ?: return
        val repo = repository ?: return
        val prod = product ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val calculator = CalculateDiscount(repo)

            val values = calculator.fillProductValues(
                anbarId = anbarId,
                product = prod.product,
                packing = packing,
                unit1ValueInput = unit1Value,
                unit2ValueInput = null,
                packingValueInput = null,
                isInput = false
            )

            unit2Value = values.unit2Value
            packingValue = values.packingValue
            Log.d("factorunit2Value2", values.unit2Value.toString())
            Log.d("factorpackingValue2", values.packingValue.toString())

        }
    }

    @Ignore
    fun setPackingValue1(value: Double, anbarId: Int) {
        packingValue = value

        val packing = packing ?: return
        val repo = repository ?: return
        val prod = product ?: return

        CoroutineScope(Dispatchers.Main).launch {

            val calculator = CalculateDiscount(repo)

            val values = calculator.fillProductValues(
                anbarId = anbarId,
                product = prod.product,
                packing = packing,
                unit1ValueInput = null,
                unit2ValueInput = null,
                packingValueInput = packingValue,
                isInput = false
            )

            unit1Value = values.unit1Value
            unit2Value = values.unit2Value
            Log.d("factorunit1Value3", values.unit1Value.toString())
            Log.d("factorunit2Value3", values.unit2Value.toString())

        }
    }
    *//*
        @Ignore
        var product: ProductWithPacking? = null


        @Ignore
        @Transient
        var factorHeader: FactorHeaderEntity? = null



    //     @Ignore
    //     fun getPackingValueFormatted(): String {
    //         val packing = packing ?: return ""
    //         if (packingValue > 0) {
    //             val remain = unit1Value % packing.unit1Value
    //             return "${packingValue.toInt()} : ${formatFloat(remain)}"
    //         }
    //         return ""
    //     }

    *//*

}*/


@Entity(
    tableName = "factor_detail_table",
    primaryKeys = ["factorId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = FactorHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FactorDetailEntity(
    val factorId: Int,
    val id: Int= 0,
    var productId: Int,
    var sortCode: Int? = null,
    var anbarId: Int? = null,
    var actId: Int? = null,
    var unit1Value: Double = 0.0,
    var unit2Value: Double = 0.0,
    var price: Double = 0.0,
    var description: String? = null,
    var packingId: Int? = null,
    var packingValue: Double = 0.0,
    var vat: Double = 0.0,
    var productSerial: Int? = null,
    var isGift: Int = 0,
    var returnCauseId: Int = 0,
    var isCanceled: Int = 0,
    var isModified: Int = 0,
    var unit1Rate: Double = 0.0,
) {
    @Ignore
    var factorDiscounts: List<FactorDiscountEntity> = emptyList()

    @Transient
    var packing: ProductPackingEntity? = null

    @Ignore
    var product: ProductWithPacking? = null

    @Ignore
    @Transient
    var repository: ProductRepository? = null

    var anbarCode: String? = null
    var anbarName: String? = null
    var productCode: String? = null
    var productName: String? = null
    var packingCode: Int? = null
    var packingName: String = ""

    @Ignore
    fun resolvePacking(): ProductPackingEntity? {
        if (packing == null && packingId != null) {
            packing = product?.packings?.firstOrNull { it.packingId == packingId }
        }
        return packing
    }

    @Ignore
    fun getPackingValueFormatted(): String {
        val packing = resolvePacking() ?: return ""
        val unitPerPack = packing.unit1Value

        if (unitPerPack <= 0) return "" // جلوگیری از تقسیم بر صفر

        // محاسبه packingValue و باقی‌مانده حتی اگر packingValue = 0 باشد
        val fullPacks = floor(unit1Value / unitPerPack)
        val remain = unit1Value % unitPerPack

        // اگر هیچ‌کدام صفر نیستند یا unit1Value > 0 باشد، نمایش بده
        if (fullPacks > 0 || remain > 0 || unit1Value > 0) {
            return "${formatFloat(remain)} : ${fullPacks.toInt()}"
        }

        return ""
    }
    @Ignore
    fun applyProduct(product: ProductWithPacking) {
        this.product = product
        this.productId = product.product.id
        this.productCode = product.product.code
        this.productName = product.product.name
        packing = null
        val defaultPack = product.packings.firstOrNull { it.isDefault == true }
        if (defaultPack != null) {
            applyPacking(defaultPack)
        }
    }

    @Ignore
    fun applyPacking(value: ProductPackingEntity?) {
        if (value == null) {
            packing = null
            packingId = null
            packingCode = null
            packingName = ""
            packingValue = 0.0
            return
        }

        if (packing == null || packing?.id != value.id) {
            packing = value
            packingId = value.packingId
            packingCode = value.packingCode
            packingName = value.packingName ?: ""
        }
    }

    @Ignore
    suspend fun setInputUnit1Value(value: Double, anbarId: Int) {
        val packing = this.packing ?: return
        val repo = repository ?: return
        val prod = product ?: return

        unit1Value = value
        val calculator = CalculateDiscount(repo)
        val values = calculator.fillProductValues(
            anbarId = anbarId,
            product = prod.product,
            packing = packing,
            unit1ValueInput = unit1Value,
            unit2ValueInput = null,
            packingValueInput = null,
            isInput = true // مهم: منبع ورودی unit1 است
        )
        unit2Value = values.unit2Value
        packingValue = values.packingValue
    }

    @Ignore
    suspend fun setInputPackingValue(value: Double, anbarId: Int) {
        val packing = this.packing ?: return
        val repo = repository ?: return
        val prod = product ?: return

        packingValue = value
        val calculator = CalculateDiscount(repo)
        val values = calculator.fillProductValues(
            anbarId = anbarId,
            product = prod.product,
            packing = packing,
            unit1ValueInput = null,
            unit2ValueInput = null,
            packingValueInput = packingValue,
            isInput = true // مهم: منبع ورودی packing است
        )
        unit1Value = values.unit1Value
        unit2Value = values.unit2Value
    }
    @Ignore
    var totalDiscountPrice: Double = 0.0

    @Ignore
    var totalAdditionalPrice: Double = 0.0

    @Ignore
    var toll: Double = 0.0

    @Ignore
    fun getPriceAfterDiscount(): Double {
        return Math.round(price + totalAdditionalPrice - totalDiscountPrice).toDouble()
    }

    @Ignore
    fun getPriceAfterVat(): Double {
        return Math.round(getPriceAfterDiscount() + vat + toll).toDouble()
    }
}