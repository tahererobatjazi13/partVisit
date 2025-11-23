package com.partsystem.partvisitapp.feature.login.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.login.repository.LoginRepository
import com.partsystem.partvisitapp.core.network.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {
    // وضعیت لاگین به‌صورت LiveData
    private val _loginUser = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginUser: LiveData<NetworkResult<LoginResponse>> = _loginUser

    /**
     * عملکرد: اجرای درخواست لاگین با استفاده از repository
     */
    fun loginUser(userName: String, password: String) {
        viewModelScope.launch {
            _loginUser.value = NetworkResult.Loading
            _loginUser.value = loginRepository.loginUser(userName, password)
        }
    }
}


