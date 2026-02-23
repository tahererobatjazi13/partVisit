package com.partsystem.partvisitapp.feature.setting.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.partsystem.partvisitapp.BuildConfig
import com.partsystem.partvisitapp.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupDarkModeSwitch()
        rxBinding()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        // app version
        val versionName = BuildConfig.VERSION_NAME
        binding.tvVersion.text = "نسخه $versionName"
    }

    private fun setupDarkModeSwitch() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDarkMode.collectLatest { enabled ->
                if (binding.swTheme.isChecked != enabled) {
                    binding.swTheme.isChecked = enabled
                }
            }
        }

        binding.swTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
        }

    }

    private fun rxBinding() {
        binding.hfSetting.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}