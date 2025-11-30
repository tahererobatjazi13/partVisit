package com.partsystem.partvisitapp.core.utils.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")
val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_ID = intPreferencesKey("key_id")
        val KEY_FIRST_NAME = stringPreferencesKey("key_firstName")
        val KEY_LAST_NAME = stringPreferencesKey("key_lastName")
        val KEY_PERSONNEL_ID = intPreferencesKey("key_personnelId")
        val KEY_IS_LOGGED = booleanPreferencesKey("key_is_login")
        val KEY_SALE_CENTER_ID = intPreferencesKey("key_sale_center_id")
        val CONTROL_VISIT_SCHEDULE = booleanPreferencesKey("key_control_visit_schedule")
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
            prefs[KEY_ID] = id
            prefs[KEY_SALE_CENTER_ID] = saleCenterId
        }
    }

    suspend fun saveControlVisitSchedule(controlVisitSchedule: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CONTROL_VISIT_SCHEDULE] = controlVisitSchedule
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


    val controlVisitScheduleFlow: Flow<Boolean?> = context.dataStore.data
        .map { it[CONTROL_VISIT_SCHEDULE] }


    suspend fun clearUserInfo() {
        context.dataStore.edit { it.clear() }
    }
}
