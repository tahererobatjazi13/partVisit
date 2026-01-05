package com.partsystem.partvisitapp.core.utils.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")
val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")
private val Context.updateDataStore by preferencesDataStore(name = "update_prefs")

@Singleton
class MainPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_ID = intPreferencesKey("key_id")
        val KEY_FIRST_NAME = stringPreferencesKey("key_firstName")
        val KEY_LAST_NAME = stringPreferencesKey("key_lastName")
        val KEY_PERSONNEL_ID = intPreferencesKey("key_personnelId")
        val KEY_IS_LOGGED = booleanPreferencesKey("key_is_login")
        val KEY_SALE_CENTER_ID = intPreferencesKey("key_sale_center_id")
        val KEY_CONTROL_VISIT_SCHEDULE = booleanPreferencesKey("key_control_visit_schedule")
        val KEY_BASE_URL = stringPreferencesKey("base_url")

        val KEY_ACT_LAST_UPDATE = stringPreferencesKey("act_last_update")
        val KEY_PATTERN_LAST_UPDATE = stringPreferencesKey("pattern_last_update")
        val KEY_PRODUCT_LAST_UPDATE = stringPreferencesKey("product_last_update")
        val KEY_DISCOUNT_LAST_UPDATE = stringPreferencesKey("discount_last_update")
    }

    suspend fun saveUserInfo(
        id: Int,
        firstName: String, lastName: String, personnelId: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID] = id
            prefs[KEY_FIRST_NAME] = firstName
            prefs[KEY_LAST_NAME] = lastName
            prefs[KEY_PERSONNEL_ID] = personnelId
            prefs[KEY_IS_LOGGED] = true
        }
    }

    suspend fun saveVisitorInfo(
        id: Int,
        saleCenterId: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SALE_CENTER_ID] = saleCenterId
        }
    }

    suspend fun saveControlVisitSchedule(controlVisitSchedule: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONTROL_VISIT_SCHEDULE] = controlVisitSchedule
        }
    }

    val id: Flow<Int?> = context.dataStore.data
        .map { it[KEY_ID] }

    val firstName: Flow<String?> = context.dataStore.data
        .map { it[KEY_FIRST_NAME] }

    val lastName: Flow<String?> = context.dataStore.data
        .map { it[KEY_LAST_NAME] }

    val personnelId: Flow<Int?> = context.dataStore.data
        .map { it[KEY_PERSONNEL_ID] }

    val isLoggedIn: Flow<Boolean?> = context.dataStore.data
        .map { it[KEY_IS_LOGGED] }

    val saleCenterId: Flow<Int?> = context.dataStore.data
        .map { it[KEY_SALE_CENTER_ID] }


    val controlVisitSchedule: Flow<Boolean?> = context.dataStore.data
        .map { it[KEY_CONTROL_VISIT_SCHEDULE] }

    suspend fun clearUserInfo() {
        context.dataStore.edit { it.clear() }
    }

    val baseUrlFlow: Flow<String?> = context.settingsDataStore.data
        .map { it[KEY_BASE_URL] }

    suspend fun saveBaseUrl(baseUrl: String) {
        context.settingsDataStore.edit {
            it[KEY_BASE_URL] = baseUrl
        }
    }

    suspend fun getBaseUrl(): String {
        return baseUrlFlow.first() ?: "http://default/api/Android/"
    }

    suspend fun setUpdatedToday(key: Preferences.Key<String>) {
        context.updateDataStore.edit { prefs ->
            prefs[key] = getTodayPersianDate()
        }
    }

    suspend fun setActUpdated() = setUpdatedToday(KEY_ACT_LAST_UPDATE)
    suspend fun setPatternUpdated() = setUpdatedToday(KEY_PATTERN_LAST_UPDATE)
    suspend fun setProductUpdated() = setUpdatedToday(KEY_PRODUCT_LAST_UPDATE)
    suspend fun setDiscountUpdated() = setUpdatedToday(KEY_DISCOUNT_LAST_UPDATE)

    private fun lastUpdateFlow(key: Preferences.Key<String>): Flow<String?> =
        context.updateDataStore.data.map { prefs ->
            prefs[key]
        }

    val actLastUpdate = lastUpdateFlow(KEY_ACT_LAST_UPDATE)
    val patternLastUpdate = lastUpdateFlow(KEY_PATTERN_LAST_UPDATE)
    val productLastUpdate = lastUpdateFlow(KEY_PRODUCT_LAST_UPDATE)
    val discountLastUpdate = lastUpdateFlow(KEY_DISCOUNT_LAST_UPDATE)

    private suspend fun isUpdatedToday(key: Preferences.Key<String>): Boolean {
        val lastDate = context.updateDataStore.data
            .map { it[key] }
            .first()

        return lastDate == getTodayPersianDate()
    }

    suspend fun hasDownloadedToday(): Boolean {
        return isUpdatedToday(KEY_ACT_LAST_UPDATE) &&
                isUpdatedToday(KEY_PATTERN_LAST_UPDATE) &&
                isUpdatedToday(KEY_PRODUCT_LAST_UPDATE) &&
                isUpdatedToday(KEY_DISCOUNT_LAST_UPDATE)
    }

    suspend fun updateTablesToday() {
        setActUpdated()
        setPatternUpdated()
        setProductUpdated()
        setDiscountUpdated()
    }
}
