package com.partsystem.partvisitapp.core.mapper

import android.content.Context
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.ApplicationSettingEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.utils.saveBase64ImageToFile
import com.partsystem.partvisitapp.feature.main.home.model.ActDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.ActDto
import com.partsystem.partvisitapp.feature.main.home.model.ApplicationSettingDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDirectionDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.GroupProductDto
import com.partsystem.partvisitapp.feature.main.home.model.InvoiceCategoryDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductImageDto

fun ApplicationSettingDto.toEntity(): ApplicationSettingEntity {
    return ApplicationSettingEntity(
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
}

fun GroupProductDto.toEntity(): GroupProductEntity {
    return GroupProductEntity(
        id = id,
        parentId = parentId,
        code = code,
        name = name,
        groupLevel = groupLevel,
        kind = kind
    )
}


fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
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
        saleRastehId = saleRastehId
    )
}

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


fun InvoiceCategoryDto.toEntity(): InvoiceCategoryEntity {
    return InvoiceCategoryEntity(
        id = id,
        code = code,
        name = name,
        kind = kind,
        fromSerial = fromSerial,
        toSerial = toSerial,
        hasVatToll = hasVatToll,
        isVatEditable = isVatEditable
    )
}

fun CustomerDto.toEntity(): CustomerEntity {
    return CustomerEntity(
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
}


fun CustomerDirectionDto.toEntity(): CustomerDirectionEntity {
    return CustomerDirectionEntity(
        id = id,
        customerId = customerId,
        fullAddress = fullAddress,
        cityName = cityName,
        mainStreet = mainStreet,
        subStreet = subStreet,
        phone1 = phone1,
        latitude = latitude,
        longitude = longitude,
        isMainAddress = isMainAddress
    )
}


fun PatternDto.toEntity(): PatternEntity {
    return PatternEntity(
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
        patternDetails = patternDetails?.toString()
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
    dataDictionaryId = dataDictionaryId
)
