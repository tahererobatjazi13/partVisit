package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.partsystem.partvisitapp.core.network.modelDto.ProductFullData
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.CalculateDiscount
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(
    tableName = "factor_detail_table",
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val factorId: Int,
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

    @Ignore
    var product: ProductWithPacking? = null

  //  fun getActId(): Int? = actId

    @Ignore
    @Transient
    var factorHeader: FactorHeaderEntity? = null
/*
    @Ignore
    suspend fun getProduct(viewModel: FactorViewModel): ProductWithPacking? {
        if (product == null && productId != null) {
            val act = getActId() ?: return null
            product = viewModel.loadProduct(productId!!, act)
        }
        return product
    }*/

    @Ignore
    @Transient
    var repository: ProductRepository? = null  // <-- Repository ذخیره شد

//    // تابع برای ست کردن Repository (یک بار کافی است)
//    fun setRepository(repo: ProductRepository) {
//        this.repository = repo
//    }

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

    @Transient
    var packing: ProductPackingEntity? = null
    var anbarCode: String? = null
    var anbarName: String? = null
    var productCode: String? = null
    var productName: String? = null
    var packingCode: Int? = null
    var packingName: String = ""


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
}
