package com.partsystem.partvisitapp.core.mapper

import android.content.Context
import com.partsystem.partvisitapp.core.database.entity.ActDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ActEntity
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
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDirectionDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.GroupProductDto
import com.partsystem.partvisitapp.feature.main.home.model.InvoiceCategoryDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductImageDto

fun GroupProductDto.toEntity(): GroupProductEntity {
    return GroupProductEntity(
        id = this.id,
        parentId = this.parentId,
        code = this.code,
        name = this.name,
        groupLevel = this.groupLevel,
        kind = this.kind
    )
}


fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        code = this.code,
        name = this.name,
        kind = this.kind,
        groupProductId = this.groupProductId,
        groupProductDetailId = this.groupProductDetailId,
        rastehId = this.rastehId,
        unitId = this.unitId,
        unit2Id = this.unit2Id,
        technicalNumber = this.technicalNumber,
        excludeVat = this.excludeVat,
        vatPercent = this.vatPercent,
        excludeToll = this.excludeToll,
        tollPercent = this.tollPercent,
        productSerial = this.productSerial,
        description = this.description,
        productKindId = this.productKindId,
        impureWeight = this.impureWeight,
        pureWeight = this.pureWeight,
        unitCode = this.unitCode,
        unitName = this.unitName,
        unit2Code = this.unit2Code,
        unit2Name = this.unit2Name,
        sabt = this.sabt,
        isSalable = this.isSalable,
        saleName = this.saleName,
        saleGroupId = this.saleGroupId,
        saleGroupDetailId = this.saleGroupDetailId,
        saleRastehId = this.saleRastehId
    )
}
suspend fun ProductImageDto.toEntity(context: Context): ProductImageEntity {
    val localPath = saveBase64ImageToFile(this.fileData, "image_${this.id}", context)
    return ProductImageEntity(
        id = this.id,
        ownerId = this.ownerId,
        tableName = this.tableName,
        fileName = this.fileName,
        fileData = this.fileData,
        localPath = localPath ?: ""
    )
}


fun InvoiceCategoryDto.toEntity(): InvoiceCategoryEntity {
    return InvoiceCategoryEntity(
        id = this.id,
        code = this.code,
        name = this.name,
        kind = this.kind,
        fromSerial = this.fromSerial,
        toSerial = this.toSerial,
        hasVatToll = this.hasVatToll,
        isVatEditable = this.isVatEditable
    )
}

fun CustomerDto.toEntity(): CustomerEntity {
    return CustomerEntity(
        id = this.id,
        code = this.code,
        name = this.name,
        groupId = this.groupId,
        groupDetailId = this.groupDetailId,
        tafsiliNationalId = this.tafsiliNationalId,
        saleCenterId = this.saleCenterId,
        degreeId = this.degreeId,
        processKindId = this.processKindId,
        customerKindId = this.customerKindId,
        isCustomerDeactive = this.isCustomerDeactive,
        deactivePersianDate = this.deactivePersianDate,
        deactiveDate = this.deactiveDate,
        customerSabt = this.customerSabt,
        tafsiliPhone1 = this.tafsiliPhone1,
        tafsiliPhone2 = this.tafsiliPhone2,
        tafsiliMobile = this.tafsiliMobile
    )
}


fun CustomerDirectionDto.toEntity(): CustomerDirectionEntity {
    return CustomerDirectionEntity(
        id = this.id,
        customerId = this.customerId,
        fullAddress = this.fullAddress,
        cityName = this.cityName,
        mainStreet = this.mainStreet,
        subStreet = this.subStreet,
        phone1 = this.phone1,
        latitude = this.latitude,
        longitude = this.longitude,
        isMainAddress = this.isMainAddress
    )
}


fun PatternDto.toEntity(): PatternEntity {
    return PatternEntity(
        id = this.id,
        code = this.code,
        name = this.name,
        createDate = this.createDate,
        persianDate = this.persianDate,
        fromDate = this.fromDate,
        fromPersianDate = this.fromPersianDate,
        toDate = this.toDate,
        toPersianDate = this.toPersianDate,
        arzId = this.arzId,
        description = this.description,
        settelmentKind = this.settelmentKind,
        creditDuration = this.creditDuration,
        discountInclusionKind = this.discountInclusionKind,
        groupInclusionKind = this.groupInclusionKind,
        centerInclusionKind = this.centerInclusionKind,
        customerInclusionKind = this.customerInclusionKind,
        processInclusionKind = this.processInclusionKind,
        regionInclusionKind = this.regionInclusionKind,
        sabt = this.sabt,
        fromSaleAmount = this.fromSaleAmount,
        toSaleAmount = this.toSaleAmount,
        hasCash = this.hasCash,
        hasMaturityCash = this.hasMaturityCash,
        hasSanad = this.hasSanad,
        hasSanadAndCash = this.hasSanadAndCash,
        hasCredit = this.hasCredit,
        dayCount = this.dayCount,
        hasAndroid = this.hasAndroid,
        patternDetails = this.patternDetails?.toString()
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
