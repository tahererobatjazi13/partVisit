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
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : Fragment() {
    @Inject
    lateinit var mainPreferences: MainPreferences

    @Inject
    lateinit var db: AppDatabase

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var customDialog: CustomDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeTheme()
        setupClicks()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() = with(binding) {
        // app version
        tvVersion.text = "نسخه ${BuildConfig.VERSION_NAME}"

        customDialog = CustomDialog()

        binding.swTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
        }

        binding.cvRemoveCache.setOnClickListener {
            showWarningDialog()
        }
    }

    private fun observeTheme() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDarkMode.collectLatest { enabled ->
                if (binding.swTheme.isChecked != enabled) {
                    binding.swTheme.isChecked = enabled
                }
            }
        }
    }

    private fun setupClicks() = binding.apply {
        hfSetting.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    private fun showWarningDialog() {

        customDialog.apply {

            setOnClickNegativeButton {
                hideProgress()
            }

            setOnClickPositiveButton {
                logout()
            }

            showDialog(
                activity,
                getString(R.string.label_warning),
                getString(R.string.error_remove_cache),
                true,
                getString(R.string.label_close),
                getString(R.string.label_confirm),
                showPositiveButton = true,
                showNegativeButton = true
            )
        }
    }

    private fun logout() {

        viewLifecycleOwner.lifecycleScope.launch {

            clearUserData()

            clearDatabase()

            findNavController().navigateUp()
        }
    }

    private suspend fun clearUserData() {
        // پاک کردن اطلاعات کاربر از DataStore
        // mainPreferences.clearUserInfo()
        mainPreferences.clearFilterConditionInfo()
    }

    private suspend fun clearDatabase() {

        with(db) {

            applicationSettingDao().clearApplicationSetting()

            visitorDao().clearVisitors()

            groupProductDao().clearGroupProduct()

            productDao().clearProducts()

            productImageDao().clearProductImage()

            productPackingDao().clearProductPacking()

            customerDao().clearCustomers()

            customerDirectionDao().clearCustomerDirection()

            assignDirectionCustomerDao().clearAssignDirectionCustomer()

            invoiceCategoryDao().clearInvoiceCategory()

            patternDao().apply {
                clearPatterns()
                clearPatternDetails()
            }

            actDao().apply {
                clearAct()
                clearActDetails()
            }

            vatDao().apply {
                clearVat()
                clearVatDetails()
            }

            saleCenterDao().apply {
                clearSaleCenters()
                clearSaleCenterAnbars()
                clearSaleCenterUsers()
            }

            discountDao().clearDiscounts()

            visitScheduleDao().apply {
                clearVisitSchedule()
                clearVisitScheduleDetails()
            }

            factorDao().apply {
                clearFactorHeader()
                clearFactorDetails()
                clearFactorDiscount()
                clearFactorGiftInfo()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}