package com.partsystem.partvisitapp.core.database.mapper

import android.content.Context
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity
import com.partsystem.partvisitapp.core.database.entity.AssignDirectionCustomerEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountCustomersEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountEshantyunsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountGiftsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountGroupsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductKindsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountProductKindInclusionsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountUsersEntity
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.MojoodiEntity
import com.partsystem.partvisitapp.core.database.entity.PatternDetailEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterAnbarEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterUserEntity
import com.partsystem.partvisitapp.core.database.entity.VatDetailEntity
import com.partsystem.partvisitapp.core.database.entity.VatEntity
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleDetailEntity
import com.partsystem.partvisitapp.core.database.entity.VisitScheduleEntity
import com.partsystem.partvisitapp.core.database.entity.VisitorEntity
import com.partsystem.partvisitapp.core.utils.saveBase64ImageToFile
import com.partsystem.partvisitapp.feature.main.home.model.ActDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.ActDto
import com.partsystem.partvisitapp.feature.main.home.model.ApplicationSettingDto
import com.partsystem.partvisitapp.feature.main.home.model.AssignDirectionCustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDirectionDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountDto
import com.partsystem.partvisitapp.feature.main.home.model.GroupProductDto
import com.partsystem.partvisitapp.feature.main.home.model.InvoiceCategoryDto
import com.partsystem.partvisitapp.feature.create_order.model.MojoodiDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductImageDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductPackingDto
import com.partsystem.partvisitapp.feature.main.home.model.SaleCenterDto
import com.partsystem.partvisitapp.feature.main.home.model.VatDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.VatDto
import com.partsystem.partvisitapp.feature.main.home.model.VisitScheduleDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.VisitScheduleDto
import com.partsystem.partvisitapp.feature.login.model.VisitorDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountCustomersDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountEshantyunsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountGiftsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountGroupsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountProductKindInclusionsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountProductKindsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountProductsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountStairsDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountUsersDto

fun ApplicationSettingDto.toEntity() = ApplicationSettingEntity(
    id = id,
    moduleId = moduleId,
    code = code,
    name = name,
    description = description,
    controlType = controlType,
    itemSource = itemSource,
    defaultValue = defaultValue,
    value = value
)

fun VisitorDto.toEntity(): VisitorEntity {
    return VisitorEntity(
        id = id,
        tafsiliFullName = tafsiliFullName,
        tafsiliLastName = tafsiliLastName,
        tafsiliFirstName = tafsiliFirstName,
        saleCenterId = saleCenterId,
        isVisitorDeactive = isVisitorDeactive,
        deactivePersianDate = deactivePersianDate,
        deactiveDate = deactiveDate,
        description = description,
        visitorSabt = visitorSabt,
        saleCenterCode = saleCenterCode,
        saleCenterName = saleCenterName,
        userId = userId
    )
}

fun VisitScheduleDto.toEntity() = VisitScheduleEntity(
    id = id,
    kind = kind,
    mainCode = mainCode,
    code = code,
    createDate = createDate,
    persianDate = persianDate,
    visitorId = visitorId,
    sabt = sabt,
    fromHour = fromHour,
    toHour = toHour
)

fun VisitScheduleDetailDto.toEntity() = VisitScheduleDetailEntity(
    id = id,
    visitScheduleId = visitScheduleId,
    sortCode = sortCode,
    directionId = directionId,
    directionDetailId = directionDetailId,
    customerId = customerId,
    pathPriority = pathPriority
)

fun GroupProductDto.toEntity() = GroupProductEntity(
    id = id,
    parentId = parentId,
    code = code,
    name = name,
    groupLevel = groupLevel,
    kind = kind
)


fun ProductDto.toEntity() = ProductEntity(
    id = id,
    code = code,
    name = name,
    kind = kind,
    groupProductId = groupProductId,
    groupProductDetailId = groupProductDetailId,
    rastehId = rastehId,
    unitId = unitId,
    unit2Id = unit2Id,
    technicalNumber = technicalNumber,
    excludeVat = excludeVat,
    vatPercent = vatPercent,
    excludeToll = excludeToll,
    tollPercent = tollPercent,
    productSerial = productSerial,
    description = description,
    productKindId = productKindId,
    impureWeight = impureWeight,
    pureWeight = pureWeight,
    unitCode = unitCode,
    unitName = unitName,
    unit2Code = unit2Code,
    unit2Name = unit2Name,
    sabt = sabt,
    isSalable = isSalable,
    saleName = saleName,
    saleGroupId = saleGroupId,
    saleGroupDetailId = saleGroupDetailId,
    saleRastehId = saleRastehId,
    convertRatio = convertRatio,
    calculateUnit2Type = calculateUnit2Type,
)

suspend fun ProductImageDto.toEntity(context: Context): ProductImageEntity {
    val localPath = saveBase64ImageToFile(fileData, "image_${id}", context)
    return ProductImageEntity(
        id = id,
        ownerId = ownerId,
        tableName = tableName,
        fileName = fileName,
        fileData = fileData,
        localPath = localPath ?: ""
    )
}

fun ProductPackingDto.toEntity() = ProductPackingEntity(
    id = id,
    productId = productId,
    packingId = packingId,
    unit1Id = unit1Id,
    unit1Value = unit1Value,
    unit2Id = unit2Id,
    unit2Value = unit2Value,
    length = length,
    width = width,
    height = height,
    volume = volume,
    weight = weight,
    isDefault = isDefault,
    isDisable = isDisable,
    packingCode = packingCode,
    packingName = packingName,
    unit1Code = unit1Code,
    unit1Name = unit1Name,
    unit2Code = unit2Code,
    unit2Name = unit2Name
)


fun CustomerDto.toEntity() = CustomerEntity(
    id = id,
    code = code,
    name = name,
    groupId = groupId,
    groupDetailId = groupDetailId,
    tafsiliNationalId = tafsiliNationalId,
    saleCenterId = saleCenterId,
    degreeId = degreeId,
    processKindId = processKindId,
    customerKindId = customerKindId,
    isCustomerDeactive = isCustomerDeactive,
    deactivePersianDate = deactivePersianDate,
    deactiveDate = deactiveDate,
    customerSabt = customerSabt,
    tafsiliPhone1 = tafsiliPhone1,
    tafsiliPhone2 = tafsiliPhone2,
    tafsiliMobile = tafsiliMobile
)

fun CustomerDirectionDto.toEntity() = CustomerDirectionEntity(
    id = id,
    customerId = customerId,
    sortCode = sortCode ?: 0,
    cityId = cityId ?: 0,
    directionId = directionId ?: 0,
    areaId = areaId ?: 0,
    directionDetailId = directionDetailId ?: 0,
    fullAddress = fullAddress ?: "",
    cityCode = cityCode ?: "",
    cityName = cityName ?: "",
    latitude = latitude,
    longitude = longitude,
    mainStreet = mainStreet ?: "",
    subStreet = subStreet ?: "",
    mahalehCode = mahalehCode ?: "",
    mahalehName = mahalehName ?: "",
    phone1 = phone1,
    phone2 = phone2,
    mobile = mobile,
    fax = fax,
    webSite = webSite,
    email = email,
    isVisit = isVisit ?: false,
    isDistribution = isDistribution ?: false,
    isPayment = isPayment ?: false,
    isActive = isActive ?: false,
    isMainAddress = isMainAddress ?: false
)

fun AssignDirectionCustomerDto.toEntity() = AssignDirectionCustomerEntity(
    id = id,
    assignDirectionId = assignDirectionId,
    tafsiliId = tafsiliId,
    mainCode = mainCode,
    code = code,
    createDate = createDate,
    persianDate = persianDate,
    isVisit = isVisit,
    isDistribution = isDistribution,
    isDemands = isDemands,
    isActive = isActive,
    customerId = customerId,
    saleCenterId = saleCenterId,
    isVisitorDeactive = isVisitorDeactive,
    customerCode = customerCode,
    customerName = customerName,
    tafsiliCode = tafsiliCode,
    tafsiliName = tafsiliName
)


fun InvoiceCategoryDto.toEntity() = InvoiceCategoryEntity(
    id = id,
    code = code,
    name = name,
    kind = kind,
    fromSerial = fromSerial,
    toSerial = toSerial,
    hasVatToll = hasVatToll,
    isVatEditable = isVatEditable
)


fun PatternDto.toEntity() = PatternEntity(
    id = id,
    code = code,
    name = name,
    createDate = createDate,
    persianDate = persianDate,
    fromDate = fromDate,
    fromPersianDate = fromPersianDate,
    toDate = toDate,
    toPersianDate = toPersianDate,
    arzId = arzId,
    description = description,
    settelmentKind = settelmentKind,
    creditDuration = creditDuration,
    discountInclusionKind = discountInclusionKind,
    groupInclusionKind = groupInclusionKind,
    centerInclusionKind = centerInclusionKind,
    customerInclusionKind = customerInclusionKind,
    processInclusionKind = processInclusionKind,
    regionInclusionKind = regionInclusionKind,
    sabt = sabt,
    fromSaleAmount = fromSaleAmount,
    toSaleAmount = toSaleAmount,
    hasCash = hasCash,
    hasMaturityCash = hasMaturityCash,
    hasSanad = hasSanad,
    hasSanadAndCash = hasSanadAndCash,
    hasCredit = hasCredit,
    dayCount = dayCount,
    hasAndroid = hasAndroid,
)

fun PatternDetailDto.toEntity(): PatternDetailEntity {
    return PatternDetailEntity(
        id = id,
        patternId = patternId,
        kind = kind,
        discountId = discountId,
        customerId = customerId,
        customerKindId = customerKindId,
        centerId = centerId,
        invoiceCategoryId = invoiceCategoryId,
        processId = processId,
        areaId = areaId,
        actId = actId,
        isDefault = isDefault,
        customerFilterKind = customerFilterKind,
        customerDegreeId = customerDegreeId,
        customerPishehId = customerPishehId,
        tafsiliGroupId = tafsiliGroupId,
        tafsiliGroupDetailId = tafsiliGroupDetailId
    )
}

fun ActDto.toEntity() = ActEntity(
    id = id,
    vatId = vatId,
    code = code,
    createDate = createDate,
    fromDate = fromDate,
    toDate = toDate,
    arzId = arzId,
    description = description,
    sabt = sabt,
    kind = kind
)

fun ActDetailDto.toEntity() = ActDetailEntity(
    id = id,
    actId = actId,
    productId = productId,
    rate = rate,
    unitKind = unitKind,
    sabt = sabt,
    useRate = useRate,
    arzRate = arzRate,
    description = description,
    saleRate = saleRate,
    dataDictionaryId = dataDictionaryId,
    rateAfterVatAndToll = rateAfterVatAndToll,
    vatPercent = vatPercent,
    tollPercent = tollPercent,
)

fun VatDto.toEntity() = VatEntity(
    id = id,
    code = code,
    createDate = createDate,
    validDate = validDate,
    serviceCalculateKind = serviceCalculateKind,
    productCalculateKind = productCalculateKind,
    vatPercent = vatPercent,
    tollPercent = tollPercent,
    description = description,
    sabt = sabt,
    kind = kind
)

fun VatDetailDto.toEntity() = VatDetailEntity(
    id = id,
    vatId = vatId,
    productId = productId,
    vatPercent = vatPercent,
    tollPercent = tollPercent,
    taxPercent = taxPercent
)

fun SaleCenterDto.toEntity(): SaleCenterEntity =
    SaleCenterEntity(id = id, code = code, name = name, saleRateKind = saleRateKind)

fun SaleCenterDto.toAnbarEntities(): List<SaleCenterAnbarEntity> =
    saleCenterAnbars.map {
        SaleCenterAnbarEntity(
            saleCenterId = it.saleCenterId,
            anbarId = it.anbarId,
            isActive = it.isActive
        )
    }

fun SaleCenterDto.toUserEntities(): List<SaleCenterUserEntity> =
    saleCenterUsers.map { SaleCenterUserEntity(saleCenterId = it.saleCenterId, userId = it.userId) }


fun DiscountDto.toEntity() = DiscountEntity(
    id = id,
    formType = formType,
    code = code,
    name = name,
    kind = kind,
    isAutoCalculate = isAutoCalculate,
    applyKind = applyKind,
    calculationKind = calculationKind,
    inclusionKind = inclusionKind,
    paymentKind = paymentKind,
    priceAmount = priceAmount,
    priceKind = priceKind,
    sabt = sabt,
    beginDate = beginDate,
    persianBeginDate = persianBeginDate,
    hasCash = hasCash,
    hasMaturityCash = hasMaturityCash,
    hasSanad = hasSanad,
    hasSanadAndCash = hasSanadAndCash,
    hasCredit = hasCredit,
    dayCount = dayCount,
    isSystem = isSystem,
    hasUseToolsPercnet = hasUseToolsPercnet,
    hasUseToolsPrice = hasUseToolsPrice,
    unitKind = unitKind,
    hasLastControl = hasLastControl,
    executeKind = executeKind,
    maxPrice = maxPrice,
    customerFilterKind = customerFilterKind,
    toDate = toDate,
    toPersianDate = toPersianDate
)

fun DiscountEshantyunsDto.toEntity() = DiscountEshantyunsEntity(
    id = id,
    discountId = discountId,
    sortCode = sortCode,
    saleUnitKind = saleUnitKind,
    fromValue = fromValue,
    toValue = toValue,
    anbarId = anbarId,
    productId = productId,
    unitKind = unitKind,
    value = value,
    packingId = packingId,
    ratio = ratio,
)

fun DiscountGiftsDto.toEntity() = DiscountGiftsEntity(
    id = id,
    discountId = discountId,
    sortCode = sortCode,
    fromPrice = fromPrice,
    toPrice = toPrice,
    anbarId = anbarId,
    productId = productId,
    unitKind = unitKind,
    value = value,
    packingId = packingId,
    ratio = ratio,
)

fun DiscountGroupsDto.toEntity() = DiscountGroupsEntity(
    id = id,
    discountId = discountId,
    kind = kind,
    groupId = groupId,
    groupDetailId = groupDetailId,
    rastehId = rastehId,
)

fun DiscountProductKindInclusionsDto.toEntity() = DiscountProductKindInclusionsEntity(
    id = id,
    discountId = discountId,
    productKindId = productKindId
)

fun DiscountProductKindsDto.toEntity() = DiscountProductKindsEntity(
    id = id,
    discountId = discountId,
    sortCode = sortCode,
    fromProductKind = fromProductKind,
    toProductKind = toProductKind,
    discountPercent = discountPercent,
    minPrice = minPrice,
)

fun DiscountProductsDto.toEntity() = DiscountProductsEntity(
    id = id,
    discountId = discountId,
    kind = kind,
    productId = productId,
)

fun DiscountStairsDto.toEntity() = DiscountStairsEntity(
    id = id,
    discountId = discountId,
    sortCode = sortCode,
    fromPrice = fromPrice,
    toPrice = toPrice,
    price = price,
    unitKind = unitKind,
    ratio = ratio,
)

fun DiscountUsersDto.toEntity() = DiscountUsersEntity(
    id = id,
    discountId = discountId,
    userId = userId,
)

fun DiscountCustomersDto.toEntity() = DiscountCustomersEntity(
    id = id,
    discountId = discountId,
    customerId = customerId,
    customerKindId = customerKindId,
    customerDegreeId = customerDegreeId,
    customerPishehId = customerPishehId,
    tafsiliGroupId = tafsiliGroupId,
    tafsiliGroupDetailId = tafsiliGroupDetailId,
    customerPriceAmount = customerPriceAmount,
    maxCustomerPriceAmount = maxCustomerPriceAmount
)

fun MojoodiDto.toEntity() = MojoodiEntity(
    anbarId = this.anbarId,
    productId = this.productId,
    productSerial = this.productSerial,
    remainValue = this.remainValue,
    remainValue2 = this.remainValue2,
    kasriValue = this.kasriValue,
    kasriValue2 = this.kasriValue2,
    mojoodi = this.mojoodi,
    mojoodi2 = this.mojoodi2
)

