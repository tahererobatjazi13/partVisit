package com.partsystem.partvisitapp.feature.login.repository

import android.content.Context
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
) {
    /**
     * عملکرد: ارسال درخواست لاگین به سرور و بازگرداندن نتیجه
     * ورودی: نام کاربری و رمز عبور
     * خروجی: <LoginResponse>
     */
    suspend fun loginUser(userName: String, password: String): NetworkResult<LoginResponse> {
        return try {

            val response = apiService.loginUser(userName, password)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }

        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }
}

