package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository

@Entity(
    tableName = "factor_detail_table",
    foreignKeys = [
        ForeignKey(
            entity = FactorHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("factorId"),
    ]
)
data class FactorDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val factorId: Int,
    var sortCode: Int? = null,
    var anbarId: Int? = null,
    var productId: Int,
    var actId: Int? = null,
    var unit1Value: Double = 0.0,
    var unit2Value: Double = 0.0,
    var price: Double = 0.0,
    var packingId: Int? = null,
    var packingValue: Double = 0.0,
    var vat: Double = 0.0,
    var productSerial: Int? = null,
    var isGift: Int = 0,
    var returnCauseId: Int = 0,
    var isCanceled: Int = 0,
    var isModified: Int = 0,
    var description: String = "",
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

    /*  fun calculateProductDiscount(q: Queries, factorDetail: FactorDetailEntity) {
          factorDetail.totalDiscountPrice = 0.0
          factorDetail.totalAdditionalPrice = 0.0
          for (factorDiscount in factorDiscounts) {
              if (factorDiscount.factorDetailId != null && factorDiscount.factorDetailId.equals(
                      factorDetail.id
                  )
              ) {
                  val discount: DiscountEntity = factorDiscount.getDiscount(q)
                  if (discount != null) {
                      if (discount.kind === DiscountKind.Discount.ordinal) {
                          factorDetail.totalDiscountPrice += factorDiscount.price
                      } else if (discount.kind === DiscountKind.Addition.ordinal) {
                          factorDetail.totalAdditionalPrice += factorDiscount.price
                      }
                  }
              }
          }
          val product: Product = factorDetail.getProduct(q)

          if (HasVatAndToll(q)) {
              factorDetail.Toll =
                  Math.round(product.TollPercent * factorDetail.getPriceAfterDiscount())
              factorDetail.Vat = Math.round(product.VatPercent * factorDetail.getPriceAfterDiscount())
          }
          q.insertFactorDetail(null, factorDetail)
      }
  */
    @Ignore
    fun getDiscountIds(level: Int, factorDetailId: Int?): ArrayList<Int> {
        val result = ArrayList<Int>()
        if (factorDiscounts != null) {
            for (factorDiscount in factorDiscounts) {
                if (level === DiscountApplyKind.ProductLevel.ordinal && factorDiscount.factorId.equals(
                        factorDetailId
                    )
                ) result.add(factorDiscount.discountId)
                else if (level === DiscountApplyKind.FactorLevel.ordinal && factorDiscount.factorId == null) result.add(
                    factorDiscount.discountId
                )
            }
        }
        return result
    }
}