package com.partsystem.partvisitapp.core.network

import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

class BaseUrlInterceptor @Inject constructor(
    private val userPreferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val baseUrlString = runBlocking {
            userPreferences.getBaseUrl()
        }

        val baseUrl = baseUrlString.toHttpUrl()

        val basePath = baseUrl.encodedPath

        val originalPath = originalRequest.url.encodedPath

        val newPath = if (basePath.endsWith("/")) {
            basePath + originalPath.removePrefix("/")
        } else {
            "$basePath/$originalPath"
        }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .encodedPath(newPath)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }}
