package com.partsystem.partvisitapp.feature.login.ui

import androidx.lifecycle.*
import com.partsystem.partvisitapp.core.network.ApiFactory
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.login.repository.LoginRepository
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.VisitorDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {


    private val _loginUser = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginUser: LiveData<NetworkResult<LoginResponse>> = _loginUser

    fun loginUser(baseUrl: String, userName: String, password: String) {
        viewModelScope.launch {
            _loginUser.value = NetworkResult.Loading
            _loginUser.value =
                loginRepository.loginUser(baseUrl, userName, password)
        }
    }

    private val _visitorState = MutableLiveData<NetworkResult<List<VisitorDto>>>()
    val visitorState: LiveData<NetworkResult<List<VisitorDto>>> = _visitorState

    fun getVisitors(visitorId:Int) {
        viewModelScope.launch {
            _visitorState.value = NetworkResult.Loading
            _visitorState.value = loginRepository.getVisitors(visitorId)
        }
    }

}


