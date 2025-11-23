package com.partsystem.partvisitapp.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.partsystem.partvisitapp.databinding.ActivitySplashBinding
import com.partsystem.partvisitapp.feature.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.BuildConfig
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.feature.login.ui.LoginActivity
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

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
        // بررسی وضعیت لاگین
        lifecycleScope.launch {
            userPreferences.isLoggedIn.collect { loggedIn ->
                if (loggedIn == true) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish()
            }
        }
    }
}
