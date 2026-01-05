package com.partsystem.partvisitapp.feature.create_order.repository

import android.util.Log
import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.DiscountDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.MojoodiDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductPackingDao
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountGiftsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairsEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import com.partsystem.partvisitapp.core.utils.CalculateUnit2Type
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.DiscountCalculationKind
import com.partsystem.partvisitapp.core.utils.DiscountExecuteKind
import com.partsystem.partvisitapp.core.utils.DiscountInclusionKind
import com.partsystem.partvisitapp.core.utils.DiscountPaymentKind
import com.partsystem.partvisitapp.core.utils.DiscountPriceKind
import com.partsystem.partvisitapp.core.utils.DiscountUnitKind
import com.partsystem.partvisitapp.core.utils.PatternInclusionKind
import com.partsystem.partvisitapp.core.utils.UnitKind
import com.partsystem.partvisitapp.core.utils.extensions.persianToGregorian
import com.partsystem.partvisitapp.feature.create_order.model.DiscountEshantyunResult
import com.partsystem.partvisitapp.feature.create_order.model.ProductModel
import com.partsystem.partvisitapp.feature.create_order.model.VwFactorDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.floor

class DiscountRepository @Inject constructor(
    private val factorDao: FactorDao,
    private val discountDao: DiscountDao,
    private val patternDao: PatternDao,
    private val productDao: ProductDao,
    private val productPackingDao: ProductPackingDao,
    private val mojoodiDao: MojoodiDao,
    private val actDao: ActDao,
) {
    suspend fun calculateDiscountInsert(
        applyKind: Int,
        factorHeader: FactorHeaderEntity,
        factorDetail: FactorDetailEntity
    ) {
        if (factorHeader.patternId == null) return

        val hasGift = false
        val insertCount: Byte = 0
        val editCount: Byte = 0 //مشخص می کند به تخفیفات/اضافات سطر اضافه شده یا خیر
        val factorSortCode = 0
        val productSortCode = 0

        val date = persianToGregorian(factorHeader.persianDate!!)
        Log.d("EshantyunapplyKind",applyKind.toString())
        Log.d("Eshantyundate",date.toString())
        var discounts = getDiscounts(applyKind, date, true)


        val pattern = patternDao.getPattern(factorHeader.patternId!!) ?: return
        val discountInclusionKind: Int = pattern.discountInclusionKind!!

        // Remove already applied discounts
        val usedDiscountIds = if (applyKind == DiscountApplyKind.ProductLevel.ordinal)
            factorDetail.getDiscountIds(applyKind, null)
        else
            factorDetail.getDiscountIds(applyKind, factorDetail.id)

        discounts = discounts.filter { !usedDiscountIds.contains(it.id) }

        // Apply pattern inclusion filter
        if (discountInclusionKind == PatternInclusionKind.List.ordinal) {
            val listDiscount = pattern.getDiscountIds()

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
        Log.d("Eshantyundiscounts",discounts.toString())
        Log.d("Eshantyundiscsizeounts",discounts.size.toString())

        processDiscounts(
            discounts = discounts,
            factor = factorHeader,
            factorDetail = factorDetail,
            applyKind = applyKind,
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
        Log.d("Eshantyun","okkkk1")

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
            Log.d("Eshantyun","okkkk111")

            if (productOfDiscount) {
                Log.d("Eshantyun","okkkk222")

                val factorDiscount = FactorDiscountEntity(
                    id = 0, // will be assigned by DB or logic
                    factorId = factor.id,
                    discountId = discount.id,
                    productId = factorDetail?.productId!!,
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
                            getSumUnit1ValueByProductIds(factor.id, productIds)
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
                    //repository.recalculateProductDiscounts(factor.id, factorDetail.id)
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


        Log.d("Eshantyun","okkkk333")

        val inclusionKind = discount.inclusionKind
        return when (inclusionKind) {
            DiscountInclusionKind.All.ordinal -> {
                Pair(true, emptyList())
            }

            DiscountInclusionKind.List.ordinal -> {
                val factorProductIds =
                    if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                        // Fetch all product IDs in factor (non-gift)
                        getFactorProductIds(factor.id)
                    } else {
                        listOf(factorDetail?.productId ?: -1)
                    }

                val discountProductIds =
                    discount.productIds // Assume this is available or fetched via DAO
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
        repository: DiscountRepository
    ) {
        var discountPrice = 0.0
        var price = 0.0
        val lastSortCode: Int = getMaxSortCode(factor.id)
        Log.d("Eshantyun","okkkk4")
        Log.d("Eshantyundiscount", discount.toString())

        // --- تعیین پایه‌ی محاسبه قیمت ---
        when (discount.priceKind) {
            DiscountPriceKind.SalePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPriceByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.price ?: 0.0
                }
            }

            DiscountPriceKind.DiscountedPrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPriceAfterDiscountByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.getPriceAfterDiscount() ?: 0.0
                }
            }

            DiscountPriceKind.PurePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPriceAfterVatByProductIds(factor.id, productIds)
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
                            getSumUnit1ValueByFactorId(factor.id)
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
                /*
                                discountPrice += calculateDiscountStairByValue(
                                    factorId = factor.id,
                                    discountId = discount.id,
                                    productIds = productIds,
                                    factorDetailId = factorDetail?.id,
                                    price = price
                                )*/
            }

            DiscountCalculationKind.Eshantyun.ordinal -> {

                Log.d("Eshantyun","okkkk3")
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

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        detail.unit1Value,
                                        null,
                                        null,
                                        false
                                    )
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.unit2Value = eshantyun.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        null,
                                        detail.unit2Value,
                                        null,
                                        false
                                    )
                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.packingId = eshantyun.packingId
                                    detail.packingValue = eshantyun.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        detail.packingId,
                                        null,
                                        null,
                                        detail.packingValue,
                                        false
                                    )
                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0

                                }
                            }

                            // ذخیره جزئیات هدیه
                            factorDao.insertFactorDetail(detail)
                            fillByAct(detail, false)

                            // ایجاد تخفیف مرتبط
                            val detailDiscount = FactorDiscountEntity(
                                id = 0,
                                factorId = factor.id,
                                discountId = discount.id,
                                productId = detail.productId,
                                factorDetailId = detail.id,
                                sortCode = 1,
                                price = detail.price,
                                discountPercent = 0.0
                            )
                            factorDao.insertFactorDiscount(detailDiscount)

                            // ایجاد FactorGiftInfo
                            val allValue =
                                getSumUnit1ValueByProductIds(factor.id, productIds)
                            if (allValue > 0) {
                                val allDetails =
                                    getFactorDetailProductIds(factor.id, productIds)
                                var maxId = getMaxFactorGiftInfoId()
                                for (itemDetail in allDetails) {
                                    val giftInfo = FactorGiftInfoEntity(
                                        id = ++maxId,
                                        factorId = factor.id,
                                        discountId = discount.id,
                                        productId = itemDetail.productId,
                                        price = kotlin.math.round(
                                            detail.price * itemDetail.unit1Value / allValue
                                        )
                                    )
                                    factorDao.insertFactorGift(giftInfo)
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
                // calculateDiscountPrice(factor, discount, price)
            }

            DiscountCalculationKind.ProductKind.ordinal -> {
                discountPrice = getDiscountByProductKind(discount.id, factor.id)
            }

            DiscountCalculationKind.Round.ordinal -> {
                val output = factor.finalPrice
                discountPrice =
                    output - (kotlin.math.round(output / discount.priceAmount) * discount.priceAmount)
            }

            DiscountCalculationKind.Gift.ordinal -> {
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

                        val gift = getDiscountGift(discount.id, allPrice)
                        if (gift != null) {

                            val detail = FactorDetailEntity(
                                id = 0,
                                factorId = factor.id,
                                productId = gift.productId,
                                sortCode = lastSortCode,
                                anbarId = gift.anbarId,
                                description = "",
                                isGift = 1,
                                unit1Value = 0.0,
                                unit2Value = 0.0,
                                packingValue = 0.0,
                                packingId = null,
                                price = 0.0
                            )

                            when (gift.unitKind) {
                                DiscountUnitKind.Unit1.ordinal -> {
                                    detail.unit1Value = gift.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        detail.unit1Value,
                                        null,
                                        null,
                                        false
                                    )
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.unit2Value = gift.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        null,
                                        detail.unit2Value,
                                        null,
                                        false
                                    )

                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.packingValue = values["PackingValue"] as? Double ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.packingId = gift.packingId
                                    detail.packingValue = gift.value
                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        detail.packingId,
                                        null,
                                        null,
                                        detail.packingValue,
                                        false
                                    )


                                    detail.unit1Value = values["Unit1Value"] as? Double ?: 0.0
                                    detail.unit2Value = values["Unit2Value"] as? Double ?: 0.0
                                }
                            }

                            factorDao.insertFactorDetail(detail)
                            fillByAct(detail, false)

                            val detailDiscount = FactorDiscountEntity(
                                id = 0,
                                factorId = factor.id,
                                discountId = discount.id,
                                productId = detail.productId,
                                factorDetailId = detail.id,
                                sortCode = 1,
                                price = detail.price,
                                discountPercent = 0.0
                            )
                            factorDao.insertFactorDiscount(detailDiscount)

                            factorDiscount.price = 0.0
                            return
                        }
                    }
                }
            }

            DiscountCalculationKind.DiscountByValue.ordinal -> {
                discountPrice += calculateDiscountByValue(
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.id,
                    price = price
                )
            }

            DiscountCalculationKind.DiscountByPrice.ordinal -> {
                val item = getDiscountStairByPrice(discount.id, price)
                if (item != null) {
                    val stairPrice =
                        if (discount.executeKind == DiscountExecuteKind.Simple.ordinal) {
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * item.price / 100.0
                            } else {
                                item.price
                            }
                        } else {
                            val multiplier = floor(price / item.ratio)
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * (item.price * multiplier) / 100.0
                            } else {
                                item.price * multiplier
                            }
                        }
                    discountPrice += stairPrice
                }
            }
        }

        // --- نهایی‌سازی ---
        factorDiscount.price = kotlin.math.round(discountPrice)
    }

    // DiscountExtensions.kt
    fun DiscountEntity.calculateStairDiscount(price: Double): Double {
        var discountPrice = 0.0

        // discountStair ممکن است null باشد (چون در Room optional است)
        this.discountStairs?.forEach { item ->
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
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple.ordinal) {
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * (itemPrice * multiplier) / 100.0
                        } else {
                            itemPrice * multiplier
                        }
                    }
                }

                price >= fromPrice && price < toPrice -> {
                    val currentPrice = price - fromPrice
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple.ordinal) {
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
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
    /* suspend fun calculateDiscountStairByValue(
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
 */
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
            /*    val finalValue = if (eshantyun.executeKind == 0) {
                    // Simple
                    eshantyun.value
                } else {
                    // Complex: floor(actualValue / ratio) * value
                    val multiplier = if (eshantyun.ratio != 0.0) {
                      floor(actualValue / eshantyun.ratio)
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
                }*/
        }

        return null
    }

    suspend fun getProduct(id: Int, includeDetail: Boolean): ProductModel? {
        val product = productDao.getProduct(id) ?: return null

        val packing = if (includeDetail) {
            productPackingDao.getPackingsByProductId(id)
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


    suspend fun fillProductValues(
        anbarId: Int?,
        productId: Int,
        factorDetailId: Int?,
        packingId: Int?,
        unit1ValueInput: Double?,
        unit2ValueInput: Double?,
        packingValueInput: Double?,
        isInput: Boolean
    ): Map<String, Double?> {

        val product = productDao.getProductByProductId(productId)
        val packing = packingId?.let {
            productPackingDao.getPackingByPackingIdAndProductId(it, productId).firstOrNull()
        }

        return fillProductValuesInternal(
            anbarId,
            product,
            packing,
            unit1ValueInput,
            unit2ValueInput,
            packingValueInput,
            isInput
        )
    }

    private suspend fun fillProductValuesInternal(
        anbarId: Int?,
        product: ProductEntity?,
        packing: ProductPackingEntity?,
        unit1Input: Double?,
        unit2Input: Double?,
        packingInput: Double?,
        isInput: Boolean
    ): Map<String, Double?> {

        var unit1Value = unit1Input
        var unit2Value = unit2Input
        var packingValue = packingInput

        val calculateUnit2Type = product?.calculateUnit2Type ?: 255
        val convertRatio = product?.convertRatio ?: 0.0
        val hasUnit2 = product?.unit2Id != null

        val packingUnit1 = packing?.unit1Value ?: 0.0
        val packingUnit2 = packing?.unit2Value ?: 0.0

        if (packingValue != null && packingValue != 0.0) {
            if (packingUnit1 != 0.0)
                unit1Value = packingUnit1 * packingValue
            else if (packingUnit2 != 0.0)
                unit2Value = packingUnit2 * packingValue
        }

        if (hasUnit2) {

            // AverageUnits
            if (calculateUnit2Type == CalculateUnit2Type.AverageUnits.ordinal &&
                !isInput && anbarId != null
            ) {
                val mojoodi = mojoodiDao.getMojoodi(anbarId, product!!.id)

                val unit1Amount = mojoodi?.remainValue ?: 1.0
                val unit2Amount = mojoodi?.remainValue2 ?: 1.0

                if (unit1Value != null && unit1Value != 0.0 && unit1Amount != 0.0) {
                    unit2Value = unit2Amount * unit1Value / unit1Amount
                } else if (unit2Value != null && unit2Value != 0.0 && unit2Amount != 0.0) {
                    unit1Value = unit1Amount * unit2Value / unit2Amount
                }
            }

            // StandardFormula
            else if (calculateUnit2Type == CalculateUnit2Type.StandardFormula.ordinal) {
                if (unit1Value != null && unit1Value != 0.0 && convertRatio != 0.0)
                    unit2Value = unit1Value * convertRatio
                else if (unit2Value != null && unit2Value != 0.0 && convertRatio != 0.0)
                    unit1Value = unit2Value / convertRatio
            }
        }

        if (packingValue == null || packingValue == 0.0) {
            if (packingUnit1 != 0.0 && unit1Value != null)
                packingValue = unit1Value / packingUnit1
            else if (packingUnit2 != 0.0 && unit2Value != null)
                packingValue = unit2Value / packingUnit2
        }

        return mapOf(
            "Unit1Value" to unit1Value,
            "Unit2Value" to unit2Value,
            "PackingValue" to packingValue
        )
    }

    fun fillByAct(insItem: FactorDetailEntity, hasArz: Boolean) {

        // اگر کاربر مصوبه زده باشد، نرخ و مبلغ پر می‌شود
        val item = actDao.getActDetail(insItem.actId!!, insItem.productId!!)

        item?.let {
            if (it.unitKind == UnitKind.Unit1.ordinal) {

                val unit1Rate = it.rate
                val calc = insItem.unit1Value * unit1Rate
                insItem.price = kotlin.math.round(calc).toDouble()

            } else {

                val unit2Rate = it.rate
                val calc = insItem.unit2Value * unit2Rate
                insItem.price = kotlin.math.round(calc).toDouble()
            }
        }
    }


    suspend fun getFactorDetailProductIds(
        factorId: Int,
        productIds: List<Int>?
    ): List<VwFactorDetail> {
        return if (productIds.isNullOrEmpty()) {
            factorDao.getFactorDetail(factorId)
        } else {
            factorDao.getFactorDetailByProductIds(factorId, productIds)
        }
    }

    /*   private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

       suspend fun calculateDiscountPrice(
           factor: FactorHeaderEntity,
           discount: DiscountEntity,
           price: Double
       ): Double = withContext(Dispatchers.Default) {
           var discountPrice = 0.0


           if (!factor.dueDate.isNullOrBlank() && !factor.createDate.isNullOrBlank()) {
               val pattern = patternDao.getPattern(factor.patternId!!) ?: return@withContext 0.0

               val dayDiff = pattern.creditDuration!!.toLong() -
                       dateDiffDays(factor.createDate!!, factor.dueDate!!)

               if (dayDiff > 0) {
                   val daysPerDiscount = discount.dayCount.toLong()
                   val fullPeriods = dayDiff / daysPerDiscount // floor division for Long
                   discountPrice += fullPeriods * discount.priceAmount * price / 100.0
               }
           }

           discountPrice
       }

       private fun dateDiffDays(date1Str: String, date2Str: String): Long {
           return try {
               val date1 = LocalDate.parse(date1Str, dateFormatter)
               val date2 = LocalDate.parse(date2Str, dateFormatter)
               date2.toEpochDay() - date1.toEpochDay()
           } catch (e: Exception) {
               0L
           }
       }*/

    suspend fun getDiscountByProductKind(discountId: Int, factorId: Int): Double {
        return discountDao.getDiscountByProductKind(discountId, factorId)
    }

    suspend fun getDiscountGift(discountId: Int, allPrice: Double): DiscountGiftsEntity? {
        return discountDao.getDiscountGift(discountId, allPrice)
    }

    suspend fun getMaxSortCode(factorId: Int): Int {
        return factorDao.getMaxSortCode(factorId)
    }

    suspend fun calculateDiscountByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>?,
        factorDetailId: Int?,
        price: Double
    ): Double {
        return discountDao.getCalculateDiscountByValue(
            factorId = factorId,
            discountId = discountId,
            productIds = productIds,
            factorDetailId = factorDetailId,
            price = price
        ) ?: 0.0
    }

    suspend fun getDiscountStairByPrice(discountId: Int, price: Double): DiscountStairsEntity? {
        return discountDao.getDiscountStairByPrice(discountId, price)
    }

    suspend fun getSumPriceByProductIds(factorId: Int, productIds: List<Int>): Double {
        // اگر لیست خالی بود، نتیجه 0 است (مثل کد جاوا)
        if (productIds.isEmpty()) return 0.0

        return factorDao.getSumPriceByProductIds(factorId, productIds) ?: 0.0
    }

    private suspend fun getSumByField(
        factorId: Int,
        productIds: List<Int>,
        fieldSelector: (FactorDetailEntity) -> Double
    ): Double {

        // اگر لیست محصولات خالی باشد، نتیجه 0 است
        if (productIds.isEmpty()) return 0.0
        val details = factorDao.getNonGiftFactorDetailsByProductIds(factorId, productIds)
        return details.sumOf(fieldSelector)
    }

    suspend fun getSumPriceAfterVatByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterVat() }

    suspend fun getSumPriceAfterDiscountByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterDiscount() }

    suspend fun getSumUnit1ValueByProductIds2(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.unit1Value }


    /**
     * معادل q.getSumUnitValueFactor(factorId) در جاوا
     */
    suspend fun getSumUnit1ValueByFactorId(factorId: Int): Double {
        return factorDao.getSumUnit1ValueByFactorId(factorId)
    }


    suspend fun getDiscounts(
        applyKind: Int,
        date: String,
        includeDetail: Boolean
    ): List<DiscountEntity> = withContext(Dispatchers.IO) {

        if (includeDetail) {
            val discountsWithDetails = discountDao.getDiscountsWithDetails(
                applyKind = applyKind,
                toDate = date,
                persianDate = date
            )
            discountsWithDetails.map { item ->
                DiscountEntity(
                    id = item.discount.id,
                    formType = item.discount.formType,
                    code = item.discount.code,
                    name = item.discount.name,
                    kind = item.discount.kind,
                    isAutoCalculate = item.discount.isAutoCalculate,
                    applyKind = item.discount.applyKind,
                    calculationKind = item.discount.calculationKind,
                    inclusionKind = item.discount.inclusionKind,
                    paymentKind = item.discount.paymentKind,
                    priceAmount = item.discount.priceAmount,
                    priceKind = item.discount.priceKind,
                    sabt = item.discount.sabt,
                    beginDate = item.discount.beginDate,
                    persianBeginDate = item.discount.persianBeginDate,
                    hasCash = item.discount.hasCash,
                    hasMaturityCash = item.discount.hasMaturityCash,
                    hasSanad = item.discount.hasSanad,
                    hasSanadAndCash = item.discount.hasSanadAndCash,
                    hasUseToolsPrice = item.discount.hasUseToolsPrice,
                    hasCredit = item.discount.hasCredit,
                    dayCount = item.discount.dayCount,
                    toDate = item.discount.toDate,
                    isSystem = item.discount.isSystem,
                    hasUseToolsPercnet = item.discount.hasUseToolsPercnet,
                    unitKind = item.discount.unitKind,
                    hasLastControl = item.discount.hasLastControl,
                    executeKind = item.discount.executeKind,
                    maxPrice = item.discount.maxPrice,
                    customerFilterKind = item.discount.customerFilterKind,
                    toPersianDate = item.discount.toPersianDate
                ).apply {
                    discountEshantyuns = discountDao.getDiscountEshantyun(item.discount.id)
                    discountGifts = discountDao.getDiscountGift(item.discount.id)
                    discountGroups = discountDao.getDiscountGroup(item.discount.id)
                    discountProducts = discountDao.getDiscountProducts(item.discount.id)
                    discountProductKinds = discountDao.getDiscountProductKind(item.discount.id)
                    discountProductKindInclusions =
                    discountDao.getDiscountProductKindInclusion(item.discount.id)
                    discountStairs = discountDao.getDiscountStair(item.discount.id)
                    discountUsers = discountDao.getDiscountUser(item.discount.id)
                }
            }
        } else {
            discountDao.getDiscounts(
                applyKind = applyKind,
                toDate = date,
                persianDate = date
            )
        }
    }
}
