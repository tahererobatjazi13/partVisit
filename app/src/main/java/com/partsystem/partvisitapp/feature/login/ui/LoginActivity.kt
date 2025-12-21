package com.partsystem.partvisitapp.feature.login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.VisitorDto
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.core.utils.hideKeyboard
import com.partsystem.partvisitapp.databinding.ActivityLoginBinding
import com.partsystem.partvisitapp.feature.login.dialog.ServerAddressDialog
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.login.model.User
import com.partsystem.partvisitapp.feature.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var loggedUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupClicks()
        observeLogin()
        observeVisitors()
        initPasswordToggle()
    }

    private fun setupClicks() {
        binding.bmbLogin.setOnClickBtnOneListener {
            if (validateInputs()) {
                doLogin()
            }
        }
        binding.bmbSetting.setOnClickBtnOneListener {
            val dialog = ServerAddressDialog()
            dialog.show(supportFragmentManager, "Setting")
        }
    }

    private fun doLogin() {
        lifecycleScope.launch {
            val baseUrl = userPreferences.baseUrlFlow.firstOrNull()

            if (baseUrl.isNullOrEmpty()) {
                showSettingDialog()
                return@launch
            }
            var userName = binding.tieUserName.text.toString().trim()
            var passWord = binding.tiePassword.text.toString().trim()

            // نرمال‌سازی و تبدیل اعداد فارسی به لاتین
            userName = convertNumbersToEnglish(fixPersianChars(userName))
            passWord = convertNumbersToEnglish(fixPersianChars(passWord))

            hideKeyboard(this@LoginActivity)
            loginViewModel.loginUser(userName, passWord)
        }
    }


    private fun showSettingDialog() {
        CustomSnackBar.make(
            findViewById(android.R.id.content),
            getString(R.string.error_configure_ip_domain_settings),
            SnackBarType.Error.value
        )?.show()

        ServerAddressDialog().show(supportFragmentManager, "Setting")
    }

    private fun observeLogin() {
        loginViewModel.loginUser.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.bmbLogin.checkShowPbOne(true)

                is NetworkResult.Success -> {
                    processLogin(result.data)
                }

                is NetworkResult.Error -> {
                    processError(result.message)
                    binding.bmbLogin.checkShowPbOne(false)
                }
            }
        }
    }

    private fun processLogin(data: LoginResponse?) {
        if (data?.isSuccess != true) {
            processError(data?.message)
            return
        }

        val user = data.listResult?.firstOrNull()
        if (user == null) {
            CustomSnackBar.make(
                findViewById(android.R.id.content),
                getString(R.string.error_user_not_found),
                SnackBarType.Error.value
            )?.show()
            return
        } else {
            loggedUser = user
            saveUserInfo(user)
            loginViewModel.getVisitors(loggedUser.personnelId)
        }
    }

    private fun saveUserInfo(user: User) {
        lifecycleScope.launch {
            userPreferences.saveUserInfo(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                personnelId = user.personnelId
            )
        }
    }

    private fun observeVisitors() {
        loginViewModel.visitorState.observe(this) { result ->
            binding.bmbLogin.checkShowPbOne(false)

            when (result) {
                is NetworkResult.Loading -> {}

                is NetworkResult.Success -> {
                    handleVisitorMatch(result.data)
                }

                is NetworkResult.Error -> {
                    processError(result.message)
                }
            }
        }
    }

    private fun handleVisitorMatch(visitors: List<VisitorDto>) {
        val personnelId = loggedUser.personnelId
        val visitor = visitors.find { it.id == personnelId }

        when {
            visitor == null -> {
                CustomSnackBar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.error_visitor_not_assigned_account),
                    SnackBarType.Error.value
                )?.show()
                return
            }

            visitor.saleCenterId == null -> {
                CustomSnackBar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.error_sales_center_not_assigned_visitor),
                    SnackBarType.Error.value
                )?.show()
                return
            }
        }

        if (visitor != null) {
            saveVisitorInfo(visitor)
        }
        Toast.makeText(this, R.string.msg_success_login, Toast.LENGTH_SHORT).show()
        navigateToHome()
    }

    private fun saveVisitorInfo(visitor: VisitorDto) {
        lifecycleScope.launch {
            userPreferences.saveVisitorInfo(
                id = visitor.id,
                saleCenterId = visitor.saleCenterId
            )
        }
    }

    /**
     * نمایش ارور به کاربر در قالب SnackBar
     */
    private fun processError(msg: String?) {
        hideKeyboard(this)
        CustomSnackBar.make(
            findViewById(android.R.id.content),
            msg ?: getString(R.string.error_unknown),
            SnackBarType.Error.value
        )?.show()
    }

    /**
     * رفتن به صفحه اصلی پس از ورود موفق
     */
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     *  تنظیم تغییر حالت آیکن پسورد در صورت خالی یا پر بودن فیلد
     */
    private fun initPasswordToggle() {
        binding.tilPassword.endIconMode = TextInputLayout.END_ICON_NONE

        binding.tiePassword.addTextChangedListener {
            binding.tilPassword.endIconMode =
                if (!it.isNullOrEmpty()) TextInputLayout.END_ICON_PASSWORD_TOGGLE
                else TextInputLayout.END_ICON_NONE
        }
    }

    /**
     * بررسی صحت ورودی‌های کاربر
     */
    private fun validateInputs(): Boolean {
        var isValid = true

        // بررسی نام کاربری
        val username = binding.tieUserName.text.toString().trim()
        binding.tilUserName.error = if (username.isEmpty()) {
            isValid = false
            getString(R.string.error_enter_username)
        } else null

        // بررسی رمز عبور
        val password = binding.tiePassword.text.toString().trim()
        binding.tilPassword.error = if (password.isEmpty()) {
            isValid = false
            getString(R.string.error_enter_password)
        } else null

        return isValid
    }
}
