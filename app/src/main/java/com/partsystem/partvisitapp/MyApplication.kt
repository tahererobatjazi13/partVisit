package com.partsystem.partvisitapp

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.partsystem.partvisitapp.feature.setting.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        observeDarkMode()
    }

    private fun observeDarkMode() {
        appScope.launch {
            settingsRepository.darkModeFlow
                .distinctUntilChanged() // فقط اگه واقعا تغییر کرده
                .collectLatest { enabled ->
                    val newMode = if (enabled)
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO

                    val currentMode = AppCompatDelegate.getDefaultNightMode()
                    Log.d("MyApplication", "darkFlow emit: $enabled | current=$currentMode | new=$newMode")

                    if (currentMode != newMode) {
                        Log.d("MyApplication", "Applying new night mode: $newMode")
                        AppCompatDelegate.setDefaultNightMode(newMode)
                    } else {
                        Log.d("MyApplication", "No change in night mode. Skipping.")
                    }
                }
        }
    }
}
