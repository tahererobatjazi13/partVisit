package com.partsystem.partvisitapp.feature.sync.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.feature.sync.model.SyncItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.utils.SyncKey
import kotlinx.coroutines.flow.first


@HiltViewModel
class SyncSelectionViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val mainPreferences: MainPreferences
) : ViewModel() {

    private val _items = MutableLiveData<List<SyncItem>>()
    val items: LiveData<List<SyncItem>> = _items

    private val _loading = MutableLiveData<String>()
    val loading: LiveData<String> = _loading

    private val _finished = MutableLiveData<Unit>()
    val finished: LiveData<Unit> = _finished

    init {
        viewModelScope.launch {
            _items.value = listOf(
                createItem(1, "تنظیمات", SyncKey.APPLICATION_SETTING),
                createItem(2, "ویزیتور", SyncKey.VISITOR),
                createItem(3, "برنامه ویزیت", SyncKey.VISIT_SCHEDULE),
                createItem(4, "گروه کالا", SyncKey.GROUP_PRODUCT),
                createItem(5, "محصولات", SyncKey.PRODUCT),
                createItem(6, "تصاویر کالا", SyncKey.PRODUCT_IMAGE),
                createItem(7, "بسته بندی", SyncKey.PRODUCT_PACKING),
                createItem(8, "مشتریان", SyncKey.CUSTOMER),
                createItem(9, "مسیر مشتری", SyncKey.CUSTOMER_DIRECTION),
                createItem(10, "تخصیص مسیر", SyncKey.ASSIGN_DIRECTION_CUSTOMER),
                createItem(11, "گروه صورتحساب", SyncKey.INVOICE_CATEGORY),
                createItem(12, "طرح فروش", SyncKey.PATTERN),
                createItem(13, "جزییات طرح", SyncKey.PATTERN_DETAIL),
                createItem(14, "مصوبه", SyncKey.ACT),
                createItem(15, "مالیات", SyncKey.VAT),
                createItem(16, "مراکز فروش", SyncKey.SALE_CENTER),
                createItem(17, "تخفیفات", SyncKey.DISCOUNT),

            )
        }
    }

    private suspend fun createItem(id: Int, title: String, key: SyncKey): SyncItem {
        return SyncItem(
            id,
            title,
            key,
            false,
            mainPreferences.getLastUpdate(key).first()
        )
    }

    fun selectAll(checked: Boolean) {
        _items.value = _items.value?.map { it.copy(isChecked = checked) }
    }

    fun updateCheck(index: Int, checked: Boolean) {
        val list = _items.value?.toMutableList() ?: return
        list[index] = list[index].copy(isChecked = checked)
        _items.value = list
    }

    fun startSync() {
        viewModelScope.launch {
            syncManager.sync(_items.value ?: emptyList()) {
                _loading.postValue(it)
            }
            _finished.postValue(Unit)
        }
    }
}
