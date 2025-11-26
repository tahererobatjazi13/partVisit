package com.partsystem.partvisitapp.feature.login.ui

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.core.utils.hideKeyboard
import com.partsystem.partvisitapp.databinding.ActivityLoginBinding
import com.partsystem.partvisitapp.feature.login.dialog.AddSettingLoginDialog
import com.partsystem.partvisitapp.feature.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initPasswordToggle()
        rxBinding()
        observeLoginResult()
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
     *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
     */
    private fun rxBinding() {
        binding.bmbLogin.setOnClickBtnOneListener {
            if (validateInputs()) {
                doLogin()
            }
        }

        binding.bmbSetting.setOnClickBtnOneListener {
            val dialog = AddSettingLoginDialog { _ ->
                // materialViewModel.insert(rawMaterial)
            }
            dialog.show(supportFragmentManager, "Setting")
        }

    }

    /**
     *  مشاهده‌ی نتایج لاگین از ViewModel و واکنش مناسب
     */
    private fun observeLoginResult() {
        loginViewModel.loginUser.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.bmbLogin.checkShowPbOne(true)

                is NetworkResult.Success -> {
                    processLogin(result.data)
                    binding.bmbLogin.checkShowPbOne(false)
                }

                is NetworkResult.Error -> {
                    processError(result.message)
                    binding.bmbLogin.checkShowPbOne(false)
                }
            }
        }
    }

    /**
     * اجرای عملیات لاگین در صورت اتصال به اینترنت
     */
    private fun doLogin() {
        var userName = binding.tieUserName.text.toString().trim()
        var passWord = binding.tiePassword.text.toString().trim()

        // نرمال‌سازی و تبدیل اعداد فارسی به لاتین
        userName = convertNumbersToEnglish(fixPersianChars(userName))
        passWord = convertNumbersToEnglish(fixPersianChars(passWord))

        hideKeyboard(this)
        loginViewModel.loginUser(userName, passWord)
    }


    /**
     * پردازش نتیجه موفق لاگین
     */
    private fun processLogin(data: LoginResponse?) {
        if (data?.isSuccess == true) {

            // ذخیره اطلاعات یوزر در DataStore
            lifecycleScope.launch {
                userPreferences.saveUserInfo(
                    id = data.listResult?.get(0)?.id ?: 0,
                    firstName = data.listResult?.get(0)?.firstName ?: "",
                    lastName = data.listResult?.get(0)?.lastName ?: "",
                    personnelId = data.listResult?.get(0)?.personnelId ?: 0,
                )
            }

            Toast.makeText(this, R.string.msg_success_login, Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            processError(data?.message)
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
