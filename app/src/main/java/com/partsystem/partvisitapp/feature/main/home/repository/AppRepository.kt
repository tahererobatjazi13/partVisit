package com.partsystem.partvisitapp.feature.main.home.repository

import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val db: AppDatabase,
    private val mainPreferences: MainPreferences
) {
    suspend fun isDatabaseReady(): Boolean {

        val hasBasicData =
            db.groupProductDao().getCount() > 0 &&
            db.productDao().getCount() > 0 &&
            db.customerDao().getCount() > 0

        return hasBasicData
    }
}
