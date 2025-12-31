package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.dao.DiscountDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.DiscountCalculationKind
import com.partsystem.partvisitapp.core.utils.DiscountExecuteKind
import com.partsystem.partvisitapp.core.utils.DiscountInclusionKind
import com.partsystem.partvisitapp.core.utils.DiscountPaymentKind
import com.partsystem.partvisitapp.core.utils.DiscountPriceKind
import com.partsystem.partvisitapp.core.utils.DiscountUnitKind
import com.partsystem.partvisitapp.core.utils.PatternInclusionKind
import com.partsystem.partvisitapp.core.utils.extensions.persianToGregorian

class DiscountRepository(
    private val factorDao: FactorDao,
    private val discountDao: DiscountDao,
    private val patternDao: PatternDao
) {
    suspend fun calculateDiscountInsert(
        applyKind: Int,
        factorHeader: FactorHeaderEntity,
        factorDetail: FactorDetailEntity
    ) {
        if (factorHeader.patternId == null) return

        val pattern = patternDao.getPattern(factorHeader.patternId!!) ?: return
        val discountInclusionKind: Int = pattern.discountInclusionKind!!

        val currentDate = persianToGregorian(factorHeader.persianDate!!)
        var discounts = discountDao.getDiscounts(applyKind, currentDate, currentDate)

        // Remove already applied discounts
        val usedDiscountIds = if (applyKind == DiscountApplyKind.ProductLevel.ordinal)
            factorDetail.getDiscountIds(applyKind, null)
        else
            factorDetail.getDiscountIds(applyKind, factorDetail.id)

        discounts = discounts.filter { !usedDiscountIds.contains(it.id) }

        // Apply pattern inclusion filter

        if (discountInclusionKind == PatternInclusionKind.List.ordinal) {
            val listDiscount = pattern.getDiscountIds() ?: arrayListOf()

            discounts = if (listDiscount.isNotEmpty()) {
                discounts.filter { it.id in listDiscount }.toMutableList()
            } else {
                mutableListOf()
            }
        }

        var lastSortCode = 1
        var hasGiftEshantyun = false

        for (discount in discounts) {
            if (discount.calculationKind == DiscountCalculationKind.Eshantyun.ordinal ||
                discount.calculationKind == DiscountCalculationKind.Gift.ordinal
            ) {
                hasGiftEshantyun = true
                //lastSortCode = factorDetail.size + 1
                break
            }
        }
        processDiscounts(
            discounts = discounts,
            factor = factorHeader,
            factorDetail = factorDetail,
            applyKind = DiscountApplyKind.ProductLevel.ordinal,
            repository = this
        )

    }


    suspend fun processDiscounts(
        discounts: List<DiscountEntity>,
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        applyKind: Int, // 0 = FactorLevel, 1 = ProductLevel
        repository: DiscountRepository
    ) {
        var factorSortCode = 0
        var productSortCode =
            0 // Note: در کد اصلی به اشتباه از factorSortCode استفاده شده در بخش else!
        var insertCount = 0
        var hasGift = false

        for (discount in discounts) {
            val (productOfDiscount, productIds) = checkProductInDiscount(
                discount,
                factor,
                factorDetail,
                applyKind
            )
            if (productOfDiscount) {
                val factorDiscount = FactorDiscountEntity(
                    id = 0, // will be assigned by DB or logic
                    discountId = discount.id,
                    factorDetailId = factorDetail?.id!!,
                    sortCode = 0, // will set below
                    price = 0.0,
                    discountPercent = 0.0
                )

                // Set SortCode
                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    if (factorSortCode == 0) {
                        val existingCount = discountDao.getFactorDiscountCountByFactorId(factor.id)
                        factorSortCode = existingCount + 1
                    }
                    factorDiscount.sortCode = factorSortCode++
                } else {
                    if (productSortCode == 0) {
                        // Important: This should count only for this FactorDetailId!
                        val existingCount = discountDao.getFactorDiscountCountByFactorDetailId(
                            factorDetail?.id ?: -1
                        )
                        productSortCode = existingCount + 1
                    }
                    factorDiscount.sortCode = productSortCode++
                }

                // Calculate discount amount

                calculate(
                    discount,
                    factorDiscount,
                    productIds,
                    applyKind,
                    factor,
                    factorDetail,
                    repository
                )

                insertCount++

                // Skip adding to factor if it's Eshantyun or Gift (they create separate FactorDetail)
                if (discount.calculationKind != DiscountCalculationKind.Eshantyun.ordinal &&
                    discount.calculationKind != DiscountCalculationKind.Gift.ordinal
                ) {
                    if (factorDiscount.price > 0) {
                        // Insert into DB and update local factor if needed
                        repository.insertFactorDiscount(factorDiscount)
                    }

                    // Handle FactorGiftInfo for Factor-Level discounts
                    if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                        val allValue =
                            repository.getSumUnit1ValueByProductIds(factor.id, productIds)
                        if (allValue > 0) {
                            var maxId = repository.getMaxFactorGiftInfoId()
                            for (productId in productIds) {
                                val unitValue =
                                    repository.getSumUnit1ValueByProduct(factor.id, productId)
                                val giftInfo = FactorGiftInfoEntity(
                                    id = ++maxId,
                                    factorId = factor.id,
                                    discountId = discount.id,
                                    price = kotlin.math.round((factorDiscount.price * unitValue) / allValue),
                                    productId = productId
                                )
                                factorDao.insertFactorGift(giftInfo)
                            }
                        }
                    }
                } else {
                    hasGift = true
                }

                // Recalculate product-level discount totals
                if (factorDetail != null) {
                    repository.recalculateProductDiscounts(factor.Id, factorDetail.Id)
                }
            }
        }
    }


    private suspend fun checkProductInDiscount(
        discount: DiscountEntity,
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        applyKind: Int
    ): Pair<Boolean, List<Int>> {
        val inclusionKind = discount.inclusionKind
        return when (inclusionKind) {
            DiscountInclusionKind.All.ordinal -> {
                Pair(true, emptyList())
            }

            DiscountInclusionKind.List.ordinal -> {
                val factorProductIds =
                    if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                        // Fetch all product IDs in factor (non-gift)
                        repository.getFactorProductIds(factor.Id)
                    } else {
                        listOf(factorDetail?.ProductId ?: -1)
                    }

                val discountProductIds =
                    discount.getProductIds() // Assume this is available or fetched via DAO
                val intersection = factorProductIds.intersect(discountProductIds.toSet())

                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    Pair(intersection.isNotEmpty(), intersection.toList())
                } else {
                    Pair(intersection.isNotEmpty(), emptyList())
                }
            }
            // Handle Group, ProductKind similarly with repository calls
            else -> {
                // For simplicity, return false — implement based on your DB structure
                Pair(false, emptyList())
            }
        }
    }

    private suspend fun calculate(
        discount: DiscountEntity,
        factorDiscount: FactorDiscountEntity,
        productIds: List<Int>,
        applyKind: Int, // 0 = FactorLevel, 1 = ProductLevel
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        repository: FactorRepository
    ) {
        var discountPrice = 0.0
        var price = 0.0

        // --- تعیین پایه‌ی محاسبه قیمت ---
        when (discount.priceKind) {
            DiscountPriceKind.SalePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    repository.getSumPriceByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.price ?: 0.0
                }
            }

            DiscountPriceKind.DiscountedPrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    repository.getSumPriceAfterDiscountByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.getPriceAfterDiscount() ?: 0.0
                }
            }

            DiscountPriceKind.PurePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    repository.getSumPriceAfterVatByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.getPriceAfterVat() ?: 0.0
                }
            }

            DiscountPriceKind.FinalPrice.ordinal -> {
                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    price = factor.finalPrice
                }
            }
        }

        // --- محاسبه بر اساس نوع تخفیف ---
        when (discount.calculationKind) {
            DiscountCalculationKind.Simple.ordinal -> {
                when (discount.paymentKind) {
                    DiscountPaymentKind.Percent.ordinal -> {
                        discountPrice = (discount.priceAmount / 100.0) * price
                    }

                    DiscountPaymentKind.Price.ordinal -> {
                        discountPrice = discount.priceAmount
                    }

                    DiscountPaymentKind.UnitPrice.ordinal -> {
                        val unitValue = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                            repository.getSumUnit1ValueByFactorId(factor.Id)
                        } else {
                            factorDetail?.unit1Value ?: 0.0
                        }
                        discountPrice = discount.priceAmount * unitValue
                    }
                }
            }

            DiscountCalculationKind.Stair.ordinal -> {
                val stairs = repository.getDiscountStairs(discount.id)
                for (item in stairs) {
                    if (price >= item.FromPrice) {
                        val currentPrice = if (price >= item.ToPrice) {
                            item.ToPrice - item.FromPrice
                        } else {
                            price - item.FromPrice
                        }

                        val stairPrice =
                            if (discount.executeKind == DiscountExecuteKind.Simple.ordinal) {
                                if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                    currentPrice * item.Price / 100.0
                                } else {
                                    item.Price
                                }
                            } else {
                                val multiplier = kotlin.math.floor(currentPrice / item.Ratio)
                                if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                    currentPrice * (item.Price * multiplier) / 100.0
                                } else {
                                    item.Price * multiplier
                                }
                            }
                        discountPrice += stairPrice
                        if (price < item.ToPrice) break
                    }
                }
            }

            DiscountCalculationKind.StairByValue.ordinal -> {
                discountPrice += repository.getCalculateDiscountStairByValue(
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.id,
                    price = price
                )
            }

            DiscountCalculationKind.Eshantyun.ordinal -> {
                if (factor.patternId != null) {
                    var actId =
                        repository.getPatternDetailActId(factor.patternId, ActKind.Product.ordinal)
                    if (actId == null) {
                        actId = repository.getPatternDetailActId(
                            factor.patternId,
                            ActKind.Service.ordinal
                        )
                    }
                    if (actId != null) {
                        val eshantyun = repository.getCalculateDiscountEshantyun(
                            factorId = factor.id,
                            discountId = discount.id,
                            productIds = productIds
                        )
                        if (eshantyun != null) {
                            val lastSortCode = repository.getMaxFactorDetailSortCode(factor.id) + 1

                            val product = repository.getProduct(eshantyun.ProductId)
                            val detail = FactorDetailEntity(
                                id = 0,
                                factorId = factor.id,
                                productId = eshantyun.ProductId,
                                sortCode = lastSortCode,
                                anbarId = eshantyun.AnbarId,
                                description = "",
                                isGift = true,
                                unit1Value = 0.0,
                                unit2Value = 0.0,
                                packingValue = 0.0,
                                packingId = null,
                                price = 0.0
                            )

                            // تنظیم مقادیر واحد بر اساس نوع
                            when (eshantyun.UnitKind) {
                                DiscountUnitKind.Unit1.ordinal -> {
                                    detail.Unit1Value = eshantyun.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.AnbarId,
                                        productId = detail.ProductId,
                                        factorDetailId = detail.Id,
                                        packingId = null,
                                        unit1Value = detail.Unit1Value,
                                        unit2Value = null,
                                        packingValue = null
                                    )
                                    detail.Unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.PackingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.Unit2Value = eshantyun.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.AnbarId,
                                        productId = detail.ProductId,
                                        factorDetailId = detail.Id,
                                        packingId = null,
                                        unit1Value = null,
                                        unit2Value = detail.unit2Value,
                                        packingValue = null
                                    )
                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.packingId = eshantyun.PackingId
                                    detail.packingValue = eshantyun.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.anbarId,
                                        productId = detail.productId,
                                        factorDetailId = detail.id,
                                        packingId = detail.packingId,
                                        unit1Value = null,
                                        unit2Value = null,
                                        packingValue = detail.packingValue
                                    )
                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.unit1Value = values["Unit2Value"] as? Double ?: 0.0
                                }
                            }

                            // ذخیره جزئیات هدیه
                            repository.insertFactorDetail(detail)
                            repository.fillByAct(detail, false)

                            // ایجاد تخفیف مرتبط
                            val detailDiscount = FactorDiscountEntity(
                                id = 0,
                                discountId = discount.id,
                                factorDetailId = detail.Id,
                                sortCode = 1,
                                price = detail.price,
                                // DiscountKind = discount.Kind
                            )
                            repository.insertFactorDiscount(detailDiscount)

                            // ایجاد FactorGiftInfo
                            val allValue =
                                repository.getSumUnit1ValueByProductIds(factor.Id, productIds)
                            if (allValue > 0) {
                                val allDetails =
                                    repository.getFactorDetailProductIds(factor.Id, productIds)
                                var maxId = repository.getMaxFactorGiftInfoId()
                                for (itemDetail in allDetails) {
                                    val giftInfo = FactorGiftInfoEntity(
                                        Id = ++maxId,
                                        FactorId = factor.id,
                                        DiscountId = discount.id,
                                        ProductId = itemDetail.ProductId,
                                        Price = kotlin.math.round(
                                            detail.Price * itemDetail.Unit1Value / allValue
                                        ).toLong()
                                    )
                                    repository.insertFactorGiftInfo(giftInfo)
                                }
                            }

                            // پس از ایجاد هدیه، این تخفیف مبلغی ندارد
                            factorDiscount.Price = 0.0
                            return
                        }
                    }
                }
            }

            DiscountCalculationKind.SettlementDate.ordinal -> {
                if (factor.DueDate?.isNotBlank() == true) {
                    val pattern = repository.getPattern(factor.PatternId!!)
                    val dayDiff = pattern.CreditDuration - DateHelper.dateDiffDay(
                        factor.CreateDate,
                        factor.DueDate
                    )
                    if (dayDiff > 0) {
                        val multiplier = kotlin.math.floor(dayDiff.toDouble() / discount.DayCount)
                        discountPrice += (multiplier * discount.PriceAmount) * price / 100.0
                    }
                }
            }

            DiscountCalculationKind.ProductKind.ordinal -> {
                discountPrice = repository.getDiscountByProductKind(discount.Id, factor.Id)
            }

            DiscountCalculationKind.Round.ordinal -> {
                val output = factor.FinalPrice
                discountPrice =
                    output - (kotlin.math.round(output / discount.PriceAmount) * discount.PriceAmount)
            }

            DiscountCalculationKind.Gift.ordinal -> {
                if (factor.PatternId != null) {
                    var actId =
                        repository.getPatternDetailActId(factor.PatternId, ActKind.Product.ordinal)
                    if (actId == null) {
                        actId = repository.getPatternDetailActId(
                            factor.PatternId,
                            ActKind.Service.ordinal
                        )
                    }
                    if (actId != null) {
                        var allPrice = 0.0
                        when (discount.PriceKind) {
                            DiscountPriceKind.SalePrice.ordinal -> {
                                allPrice = repository.getSumPriceByProductIds(factor.Id, productIds)
                            }

                            DiscountPriceKind.DiscountedPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterDiscountByProductIds(
                                    factor.Id,
                                    productIds
                                )
                            }

                            DiscountPriceKind.PurePrice.ordinal,
                            DiscountPriceKind.FinalPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterVatByProductIds(
                                    factor.Id,
                                    productIds
                                )
                            }
                        }

                        val gift = repository.getDiscountGift(discount.Id, allPrice)
                        if (gift != null) {
                            val lastSortCode = repository.getMaxFactorDetailSortCode(factor.Id) + 1

                            val detail = FactorDetailEntity(
                                Id = 0,
                                FactorId = factor.id,
                                ProductId = gift.ProductId,
                                SortCode = lastSortCode,
                                AnbarId = gift.AnbarId,
                                Description = "",
                                IsGift = true,
                                Unit1Value = 0.0,
                                Unit2Value = 0.0,
                                PackingValue = 0.0,
                                PackingId = null,
                                Price = 0.0
                            )

                            when (gift.UnitKind) {
                                DiscountUnitKind.Unit1.ordinal -> {
                                    detail.Unit1Value = gift.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.AnbarId,
                                        productId = detail.ProductId,
                                        factorDetailId = detail.Id,
                                        packingId = null,
                                        unit1Value = detail.Unit1Value,
                                        unit2Value = null,
                                        packingValue = null
                                    )
                                    detail.Unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.PackingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.Unit2Value = gift.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.AnbarId,
                                        productId = detail.ProductId,
                                        factorDetailId = detail.Id,
                                        packingId = null,
                                        unit1Value = null,
                                        unit2Value = detail.Unit2Value,
                                        packingValue = null
                                    )
                                    detail.Unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.PackingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.PackingId = gift.PackingId
                                    detail.PackingValue = gift.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.AnbarId,
                                        productId = detail.ProductId,
                                        factorDetailId = detail.Id,
                                        packingId = detail.PackingId,
                                        unit1Value = null,
                                        unit2Value = null,
                                        packingValue = detail.PackingValue
                                    )
                                    detail.Unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.Unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                }
                            }

                            repository.insertFactorDetail(detail)
                            repository.fillByAct(detail, false)

                            val detailDiscount = FactorDiscountEntity(
                                Id = 0,
                                DiscountId = discount.id,
                                FactorDetailId = detail.Id,
                                SortCode = 1,
                                Price = detail.Price,
                                DiscountKind = discount.Kind
                            )
                            repository.insertFactorDiscount(detailDiscount)

                            factorDiscount.Price = 0.0
                            return
                        }
                    }
                }
            }

            DiscountCalculationKind.DiscountByValue.ordinal -> {
                discountPrice += repository.getCalculateDiscountByValue(
                    factorId = factor.Id,
                    discountId = discount.Id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.Id,
                    price = price
                )
            }

            DiscountCalculationKind.DiscountByPrice.ordinal -> {
                val item = repository.getDiscountStairByPrice(discount.Id, price)
                if (item != null) {
                    val stairPrice =
                        if (discount.ExecuteKind == DiscountExecuteKind.Simple.ordinal) {
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * item.Price / 100.0
                            } else {
                                item.Price
                            }
                        } else {
                            val multiplier = kotlin.math.floor(price / item.Ratio)
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * (item.Price * multiplier) / 100.0
                            } else {
                                item.Price * multiplier
                            }
                        }
                    discountPrice += stairPrice
                }
            }
        }

        // --- نهایی‌سازی ---
        factorDiscount.Price = kotlin.math.round(discountPrice)
    }


    suspend fun getFactorDiscountCount2(factorDetailId: Int): Int {
        return discountDao.getFactorDiscountCountByFactorDetailId(factorDetailId)
    }

    suspend fun insertFactorDiscount(discount: FactorDiscountEntity) {
        discountDao.insertFactorDiscount(discount)
    }

    suspend fun getSumUnit1ValueByProductIds(factorId: Int, productIds: List<Int>): Double {
        return discountDao.getSumUnit1ValueByProductIds(factorId, productIds) ?: 0.0
    }

    suspend fun getSumUnit1ValueByProduct(factorId: Int, productId: Int): Double {
        return discountDao.getSumUnit1ValueByProduct(factorId, productId) ?: 0.0
    }

    suspend fun getMaxFactorGiftInfoId(): Int {
        return discountDao.getMaxFactorGiftInfoId() ?: 0
    }

    suspend fun insertFactorGiftInfo(info: FactorGiftInfoEntity) {
        discountDao.insertFactorGiftInfo(info)
    }

    suspend fun getFactorProductIds(factorId: Int): List<Int> {
        return discountDao.getFactorProductIds(factorId)
    }

    suspend fun getAppliedDiscountIds(factorId: Int, factorDetailId: Int): List<Int> {
        return discountDao.getAppliedDiscountIds(factorId, factorDetailId)
    }

    // --- بازمحاسبه تخفیف‌های سطح محصول ---
    suspend fun recalculateProductLevelDiscounts(factorId: Int, factorDetailId: Int) {
        // 1. جمع تخفیف‌ها و اضافات
        //   val discounts = discountDao.getDiscountsForFactorDetail(factorDetailId)
        var totalDiscount = 0.0
        var totalAddition = 0.0

        for (fd in discounts) {
            val discount = TODO("Fetch Discount by fd.DiscountId") // نیاز به DiscountDao
            //  if (discount.Kind == 0) totalDiscount += fd.Price
            else totalAddition += fd.Price
        }

        // 2. به‌روزرسانی FactorDetail
        TODO("Update FactorDetail.TotalDiscountPrice and TotalAdditionalPrice in DB")
    }


}