package com.partsystem.partvisitapp.feature.sync.ui

import com.partsystem.partvisitapp.core.utils.SyncKey
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.feature.main.home.repository.HomeRepository
import com.partsystem.partvisitapp.feature.sync.model.SyncItem
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val repository: HomeRepository,
    private val mainPreferences: MainPreferences
) {
    suspend fun sync(
        items: List<SyncItem>,
        onLoading: (String) -> Unit
    ) {
        for (item in items) {
            if (!item.isChecked) continue

            onLoading("در حال دریافت ${item.title} ...")

            // دانلود داده
            when (item.key) {
                SyncKey.APPLICATION_SETTING -> repository.fetchAndSaveApplicationSetting()
                SyncKey.VISITOR -> repository.fetchAndSaveVisitors()
                SyncKey.VISIT_SCHEDULE -> repository.fetchAndSaveVisitSchedules()
                SyncKey.GROUP_PRODUCT -> repository.fetchAndSaveGroups()
                SyncKey.PRODUCT -> repository.fetchAndSaveProducts()
                SyncKey.PRODUCT_IMAGE -> repository.fetchAndSaveProductImages()
                SyncKey.PRODUCT_PACKING -> repository.fetchAndSaveProductPacking()
                SyncKey.CUSTOMER -> repository.fetchAndSaveCustomers()
                SyncKey.CUSTOMER_DIRECTION -> repository.fetchAndSaveCustomerDirections()
                SyncKey.ASSIGN_DIRECTION_CUSTOMER -> repository.fetchAndSaveAssignDirectionCustomer()
                SyncKey.INVOICE_CATEGORY -> repository.fetchAndSaveInvoiceCategory()
                SyncKey.PATTERN -> repository.fetchAndSavePattern()
                SyncKey.PATTERN_DETAIL -> repository.fetchAndSavePatternDetails()
                SyncKey.ACT -> repository.fetchAndSaveAct()
                SyncKey.VAT -> repository.fetchAndSaveVat()
                SyncKey.SALE_CENTER -> repository.fetchAndSaveSaleCenter()
                SyncKey.DISCOUNT -> repository.fetchAndSaveDiscount()
            }

            // ست کردن زمان آپدیت
            mainPreferences.setLastUpdate(item.key, getTodayPersianDate())

            // ست شدن آپدیت ۴ جدول حیاتی
            when (item.key) {
                SyncKey.ACT -> mainPreferences.setActUpdated()
                SyncKey.PATTERN -> mainPreferences.setPatternUpdated()
                SyncKey.PRODUCT -> mainPreferences.setProductUpdated()
                SyncKey.DISCOUNT -> mainPreferences.setDiscountUpdated()
                else -> {}
            }
        }
    }

}
