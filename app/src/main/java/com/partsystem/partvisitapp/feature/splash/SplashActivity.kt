package com.partsystem.partvisitapp.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.partsystem.partvisitapp.databinding.ActivitySplashBinding
import com.partsystem.partvisitapp.feature.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.BuildConfig
import com.partsystem.partvisitapp.core.base.BaseActivity
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.feature.login.ui.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        checkLoginStatus()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        // app version
        val versionName = BuildConfig.VERSION_NAME
        binding.tvVersion.text = "نسخه $versionName"
    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            val loggedIn = mainPreferences.isLoggedIn.first()

            if (loggedIn == true) {
                navigateToMain()
            } else {
                navigateToLogin()
            }
        }
    }

    // مثال در SplashActivity.kt
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
