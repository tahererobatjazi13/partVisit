package com.partsystem.partvisitapp.feature.setting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.feature.setting.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    /** حالت دارک مود به‌صورت StateFlow برای UI */
    val isDarkMode = repository.darkModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    /** تغییر حالت دارک مود */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }
}
