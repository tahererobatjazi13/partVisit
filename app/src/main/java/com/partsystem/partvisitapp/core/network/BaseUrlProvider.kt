package com.partsystem.partvisitapp.core.network

import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import javax.inject.Inject
import javax.inject.Singleton

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Singleton
class BaseUrlProvider @Inject constructor(
    private val mainPreferences: MainPreferences
) {

    @Volatile
    private var cachedBaseUrl: HttpUrl? = null

    suspend fun init() {
        val url = mainPreferences.getBaseUrl()
        cachedBaseUrl = url.toHttpUrl()
    }

    fun getBaseUrl(): HttpUrl {
        return cachedBaseUrl
            ?: throw IllegalStateException("BaseUrlProvider is not initialized")
    }

    fun updateBaseUrl(newUrl: String) {
        cachedBaseUrl = newUrl.toHttpUrl()
    }
}
