package com.partsystem.partvisitapp.feature.create_order.repository

import com.partsystem.partvisitapp.core.database.dao.DiscountDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
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
import com.partsystem.partvisitapp.feature.create_order.model.DiscountEshantyunResult
import com.partsystem.partvisitapp.feature.create_order.model.ProductModel
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import kotlinx.coroutines.withContext
import kotlin.math.floor

class DiscountRepository(
    private val factorDao: FactorDao,
    private val discountDao: DiscountDao,
    private val patternDao: PatternDao,
    private val productDao: ProductDao,
    private val pProductPackingDao: ProductPackingDao

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
                            repository.getSumUnit1ValueByFactorId(factor.id)
                        } else {
                            factorDetail?.unit1Value ?: 0.0
                        }
                        discountPrice = discount.priceAmount * unitValue
                    }
                }
            }

            DiscountCalculationKind.Stair.ordinal -> {
                val discountAmount = discount.calculateStairDiscount(price)
            }

            DiscountCalculationKind.StairByValue.ordinal -> {

                discountPrice += calculateDiscountStairByValue(
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
                        factorDao.getPatternDetailActId(factor.patternId!!, ActKind.Product.ordinal)
                    if (actId == null) {
                        actId = factorDao.getPatternDetailActId(
                            factor.patternId!!,
                            ActKind.Service.ordinal
                        )
                    }
                    if (actId != null) {
                        val eshantyun = getCalculateDiscountEshantyun(
                            factorId = factor.id,
                            discountId = discount.id,
                            productIds = productIds
                        )
                        if (eshantyun != null) {
                           // val lastSortCode = factorDetail.maxOfOrNull { it.sortCode } ?: 0

                           // val product = getProduct(eshantyun.productId!!,true)
                            val detail = FactorDetailEntity(
                                id = 0,
                                factorId = factor.id,
                                productId = eshantyun.productId!!,
                                sortCode = lastSortCode,
                                anbarId = eshantyun.anbarId,
                                description = "",
                                isGift = 1,
                                unit1Value = 0.0,
                                unit2Value = 0.0,
                                packingValue = 0.0,
                                packingId = null,
                                price = 0.0
                            )

                            // تنظیم مقادیر واحد بر اساس نوع
                            when (eshantyun.unitKind) {
                                DiscountUnitKind.Unit1.ordinal -> {
                                    detail.unit1Value = eshantyun.value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.anbarId,
                                        productId = detail.productId,
                                        factorDetailId = detail.id,
                                        packingId = null,
                                        unit1Value = detail.un,
                                        unit2Value = null,
                                        packingValue = null
                                    )
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.unit2Value = eshantyun.value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.anbarId,
                                        productId = detail.productId,
                                        factorDetailId = detail.id,
                                        packingId = null,
                                        unit1Value = null,
                                        unit2Value = detail.unit2Value,
                                        packingValue = null
                                    )
                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.packingId = eshantyun.packingId
                                    detail.packingValue = eshantyun.value
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
                                factorDetailId = detail.id,
                                sortCode = 1,
                                price = detail.price,
                                // DiscountKind = discount.Kind
                            )
                            repository.insertFactorDiscount(detailDiscount)

                            // ایجاد FactorGiftInfo
                            val allValue =
                                repository.getSumUnit1ValueByProductIds(factor.id, productIds)
                            if (allValue > 0) {
                                val allDetails =
                                    repository.getFactorDetailProductIds(factor.id, productIds)
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
                            factorDiscount.price = 0.0
                            return
                        }
                    }
                }
            }

            DiscountCalculationKind.SettlementDate.ordinal -> {
                if (factor.dueDate?.isNotBlank() == true) {
                    val pattern = repository.getPattern(factor.patternId!!)
                    val dayDiff = pattern.CreditDuration - DateHelper.dateDiffDay(
                        factor.createDate,
                        factor.dueDate
                    )
                    if (dayDiff > 0) {
                        val multiplier = kotlin.math.floor(dayDiff.toDouble() / discount.dayCount)
                        discountPrice += (multiplier * discount.priceAmount) * price / 100.0
                    }
                }
            }

            DiscountCalculationKind.ProductKind.ordinal -> {
                discountPrice = repository.getDiscountByProductKind(discount.id, factor.id)
            }

            DiscountCalculationKind.Round.ordinal -> {
                val output = factor.finalPrice
                discountPrice =
                    output - (kotlin.math.round(output / discount.priceAmount) * discount.priceAmount)
            }

            DiscountCalculationKind.Gift.ordinal -> {
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
                        var allPrice = 0.0
                        when (discount.priceKind) {
                            DiscountPriceKind.SalePrice.ordinal -> {
                                allPrice = repository.getSumPriceByProductIds(factor.id, productIds)
                            }

                            DiscountPriceKind.DiscountedPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterDiscountByProductIds(
                                    factor.id,
                                    productIds
                                )
                            }

                            DiscountPriceKind.PurePrice.ordinal,
                            DiscountPriceKind.FinalPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterVatByProductIds(
                                    factor.id,
                                    productIds
                                )
                            }
                        }

                        val gift = repository.getDiscountGift(discount.id, allPrice)
                        if (gift != null) {
                            val lastSortCode = repository.getMaxFactorDetailSortCode(factor.id) + 1

                            val detail = FactorDetailEntity(
                                id = 0,
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
                                    detail.unit1Value = gift.Value
                                    val values = repository.fillProductValues(
                                        anbarId = detail.anbarId,
                                        productId = detail.productId,
                                        factorDetailId = detail.id,
                                        packingId = null,
                                        unit1Value = detail.unit1Value,
                                        unit2Value = null,
                                        packingValue = null
                                    )
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.unit2Value = gift.Value
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
                                FactorDetailId = detail.id,
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
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.id,
                    price = price
                )
            }

            DiscountCalculationKind.DiscountByPrice.ordinal -> {
                val item = repository.getDiscountStairByPrice(discount.id, price)
                if (item != null) {
                    val stairPrice =
                        if (discount.executeKind == DiscountExecuteKind.Simple.ordinal) {
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


    // DiscountExtensions.kt

    fun DiscountEntity.calculateStairDiscount(price: Double): Double {
        var discountPrice = 0.0

        // discountStair ممکن است null باشد (چون در Room optional است)
        this.discountStair?.forEach { item ->
            // اگر item null باشد (مثلاً به دلیل left join یا اشتباه دیتابیس)، نادیده بگیر
            item ?: return@forEach

            val fromPrice = item.fromPrice
            val toPrice = item.toPrice
            val itemPrice = item.price
            val ratio = item.ratio
            val executeKind = this.executeKind
            val paymentKind = this.paymentKind

            when {
                price >= fromPrice && price >= toPrice -> {
                    val currentPrice = toPrice - fromPrice
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple) {
                        if (paymentKind == DiscountPaymentKind.Percent) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent) {
                            currentPrice * (itemPrice * multiplier) / 100.0
                        } else {
                            itemPrice * multiplier
                        }
                    }
                }

                price >= fromPrice && price < toPrice -> {
                    val currentPrice = price - fromPrice
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple) {
                        if (paymentKind == DiscountPaymentKind.Percent) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent) {
                            currentPrice * (itemPrice * multiplier) / 100.0
                        } else {
                            itemPrice * multiplier
                        }
                    }
                }
            }
        }

        return discountPrice
    }

    /**
     * محاسبه تخفیف پله‌ای بر اساس فاکتور، تخفیف، محصولات و قیمت مرجع
     *
     * معادل کامل:
     * getCalculateDiscountStairByValue(factorId, discountId, productIds, factorDetailId, price)
     */
    suspend fun calculateDiscountStairByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>? = null,
        factorDetailId: Int? = null,
        price: Double
    ): Double = withContext(ioDispatcher) {
        // ۱. دریافت تخفیف + پله‌ها
        val discount = factorDao.getDiscountWithStairs(discountId) ?: return@withContext 0.0

        // ۲. دریافت جزئیات فاکتور مطابق فیلتر
        val factorDetails = when {
            factorDetailId != null -> {
                val detail = factorDao.getDetailById(factorId, factorDetailId)
                listOfNotNull(detail)
            }

            !productIds.isNullOrEmpty() -> {
                factorDao.getDetailsByFactorAndProducts(factorId, productIds)
            }

            else -> {
                factorDao.getDetailsByFactorId(factorId)
            }
        }

        if (factorDetails.isEmpty()) return@withContext 0.0

        // ۳. محاسبه مجموع مقادیر واحد (مثل جدول tbl در کوئری)
        val totalUnit1 = factorDetails.sumOf { it.unit1Value }
        val totalUnit2 = factorDetails.sumOf { it.unit2Value }
        val totalPacking = factorDetails.sumOf { it.packingValue }

        // ۴. محاسبه تخفیف بر اساس هر پله
        var totalDiscount = 0.0

        for (stair in discount.discountStair.orEmpty()) {
            if (stair == null) continue

            // تعیین Val بر اساس UnitKind (0=Unit1, 1=Unit2, 2=Packing)
            val valAmount = when (stair.unitKind) {
                0 -> totalUnit1
                1 -> totalUnit2
                2 -> totalPacking
                else -> 0.0
            }

            if (valAmount == 0.0) continue // معادل NULLIF در SQL

            // تشخیص بازه مؤثر
            val effectiveRange = when {
                valAmount >= stair.toPrice -> stair.toPrice - stair.fromPrice
                valAmount >= stair.fromPrice -> valAmount - stair.fromPrice
                else -> 0.0
            }

            if (effectiveRange <= 0) continue

            // محاسبه مبلغ تخفیف
            val discountValue = if (discount.paymentKind == 1) {
                // PaymentKind = 1 → Amount (مبلغ ثابت)
                if (discount.executeKind == 0) {
                    // Simple
                    stair.price
                } else {
                    // Complex
                    val multiplier =
                        if (stair.ratio != 0.0) floor(effectiveRange / stair.ratio) else 0.0
                    stair.price * multiplier
                }
            } else {
                // PaymentKind = 0 → Percent
                val baseValue = price / valAmount // معادل (SumPrice / Val)
                val multiplier = if (discount.executeKind == 0) {
                    effectiveRange
                } else {
                    if (stair.ratio != 0.0) floor(effectiveRange / stair.ratio) else 0.0
                }
                baseValue * multiplier * stair.price / 100.0
            }

            totalDiscount += discountValue
        }

        totalDiscount
    }


    suspend fun getCalculateDiscountEshantyun(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>? = null
    ): DiscountEshantyunResult? {
        // ۱. جزئیات فاکتور را بخوان
        val factorDetails = if (productIds.isNullOrEmpty()) {
            factorDao.getDetailsByFactorId(factorId)
        } else {
            factorDao.getDetailsByFactorAndProducts(factorId, productIds)
        }

        if (factorDetails.isEmpty()) return null

        // ۲. مقادیر جمع‌بندی شده را محاسبه کن
        val totalUnit1 = factorDetails.sumOf { it.unit1Value }
        val totalUnit2 = factorDetails.sumOf { it.unit2Value }
        val totalPacking = factorDetails.sumOf { it.packingValue }

        // ۳. تمام DiscountEshantyun مربوط به این تخفیف را بخوان
        val eshantyuns = factorDao.getEshantyunsByDiscountId(discountId)

        // ۴. تخفیف معتبر را پیدا کن (اولین موردی که شرط BETWEEN را دارد و Value > 0)
        for (eshantyun in eshantyuns) {
            val (isInRange, actualValue) = when (eshantyun.saleUnitKind) {
                0 -> {
                    val v = totalUnit1
                    (v >= eshantyun.fromValue && v <= eshantyun.toValue) to v
                }

                1 -> {
                    val v = totalUnit2
                    (v >= eshantyun.fromValue && v <= eshantyun.toValue) to v
                }

                2 -> {
                    val v = totalPacking
                    (v >= eshantyun.fromValue && v <= eshantyun.toValue) to v
                }

                else -> false to 0.0
            }

            if (!isInRange) continue

            // محاسبه نهایی Value — دقیقاً معادل کوئری شما
            val finalValue = if (eshantyun.executeKind == 0) {
                // Simple
                eshantyun.value
            } else {
                // Complex: floor(actualValue / ratio) * value
                val multiplier = if (eshantyun.ratio != 0.0) {
                    kotlin.math.floor(actualValue / eshantyun.ratio)
                } else {
                    0.0
                }
                multiplier * eshantyun.value
            }

            // فقط اگر Value > 0 باشد
            if (finalValue > 0) {
                return DiscountEshantyunResult(
                    anbarId = eshantyun.anbarId,
                    productId = eshantyun.productId,
                    packingId = eshantyun.packingId,
                    unitKind = eshantyun.unitKind,
                    value = finalValue
                )
            }
        }

        return null
    }

    suspend fun getProduct(id: Int, includeDetail: Boolean): ProductModel? {
        val product = productDao.getProduct(id) ?: return null

        val packing = if (includeDetail) {
            pProductPackingDao.getProductPacking(id)
        } else {
            emptyList()
        }

        return ProductModel(
            id = product.id,
            name = product.name!!,
            code = product.code!!,
            productPacking = packing
        )
    }

//    suspend fun getProduct(id: Int, includeDetail: Boolean): ProductModel? {
//        if (includeDetail) {
//            val productWithPacking = productDao.getProductWithPacking(id)
//            return productWithPacking?.let {
//                ProductModel(
//                    id = it.product.id,
//                    name = it.product.name,
//                    code = it.product.code,
//                    productPacking = it.packings
//                )
//            }
//        } else {
//            val product = productDao.getProduct(id)
//            return product?.let {
//                ProductModel(
//                    id = it.id,
//                    name = it.name,
//                    code = it.code,
//                    productPacking = emptyList() // یا null
//                )
//            }
//        }
//
//    }


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
/*
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
*/


}