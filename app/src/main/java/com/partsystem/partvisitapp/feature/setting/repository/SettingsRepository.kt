package com.partsystem.partvisitapp.feature.setting.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.partsystem.partvisitapp.core.utils.datastore.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    //خواندن حالت دارک مود از DataStore
    val darkModeFlow: Flow<Boolean> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[DARK_MODE_KEY] ?: false }

    // ذخیره حالت دارک مود
    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}
