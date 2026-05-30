package com.partsystem.partvisitapp.feature.sync.model

import com.partsystem.partvisitapp.core.utils.SyncKey

data class SyncItem(
    val id: Int,
    val title: String,
    val key: SyncKey,
    val isChecked: Boolean = false,
    val lastUpdate: String? = null
)