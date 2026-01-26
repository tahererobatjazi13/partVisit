package com.partsystem.partvisitapp.feature.main.home.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.isInternetAvailable
import com.partsystem.partvisitapp.databinding.DialogLoadingBinding
import com.partsystem.partvisitapp.databinding.FragmentHomeBinding
import com.partsystem.partvisitapp.feature.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    @Inject
    lateinit var db: AppDatabase

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var customDialog: CustomDialog? = null

    private var loadingCount = 0
    private lateinit var loadingDialog: Dialog
    private lateinit var loadingBinding: DialogLoadingBinding

    private val tasks = mutableListOf<() -> Unit>()
    private var currentTaskIndex = 0
    private var doubleBackToExit = false
    private var customDialogForceUpdate: CustomDialog? = null
    private var syncType = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initLoadingDialog()
        initAdapter()
        setupClicks()
        handleBackToExit()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {

        customDialog = CustomDialog()
        customDialogForceUpdate = CustomDialog()
        binding.tvDate.text = getTodayPersianDate()

        // جمع‌آوری دیتا از DataStore
        lifecycleScope.launch {
            combine(
                mainPreferences.firstName,
                mainPreferences.lastName
            ) { firstName, lastName ->
                "${firstName ?: ""} ${lastName ?: ""}"
            }.collect { fullName ->
                binding.tvVisitorName.text = fullName
            }
        }
    }

    private fun setupObserver() {
        tasks.clear()
        currentTaskIndex = 0

        addTask(
            liveData = viewModel.applicationSetting,
            loadingMsg = "$syncType تنظیمات ..."
        ) { viewModel.fetchApplicationSetting() }

        addTask(
            liveData = viewModel.visitor,
            loadingMsg = "$syncType ویزیتور ..."
        ) { viewModel.fetchVisitors() }

        addTask(
            liveData = viewModel.visitSchedule,
            loadingMsg = "$syncType برنامه ویزیت ..."
        ) { viewModel.fetchVisitSchedules() }

        addTask(
            liveData = viewModel.groupProducts,
            loadingMsg = "$syncType گروه کالا ..."
        ) { viewModel.fetchGroupProducts() }

        addTask(
            viewModel.products,
            loadingMsg = "$syncType محصولات ..."
        ) { viewModel.fetchProducts() }

        addTask(
            liveData = viewModel.productImages,
            loadingMsg = "$syncType تصاویر کالا ..."
        ) { viewModel.fetchProductImages() }

        addTask(
            liveData = viewModel.productPacking,
            loadingMsg = "$syncType بسته‌بندی ..."
        ) { viewModel.fetchProductPacking() }

        addTask(
            liveData = viewModel.customers,
            loadingMsg = "$syncType مشتریان ..."
        ) { viewModel.fetchCustomers() }

        addTask(
            liveData = viewModel.customerDirections,
            loadingMsg = "$syncType مسیر مشتریان ..."
        ) { viewModel.fetchCustomerDirections() }

        addTask(
            liveData = viewModel.assignDirectionCustomer,
            loadingMsg = "$syncType مسیر مشتری ..."
        ) { viewModel.fetchAssignDirectionCustomer() }

        addTask(
            liveData = viewModel.invoiceCategory,
            loadingMsg = "$syncType گروه صورتحساب ..."
        ) { viewModel.fetchInvoiceCategory() }

        addTask(
            liveData = viewModel.pattern,
            loadingMsg = "$syncType طرح فروش ..."
        ) { viewModel.fetchPattern() }

        addTask(
            liveData = viewModel.patternDetails,
            loadingMsg = "$syncType جزییات طرح فروش ..."
        ) { viewModel.fetchPatternDetails() }

        addTask(
            liveData = viewModel.act,
            loadingMsg = "$syncType مصوبه ..."
        ) { viewModel.fetchAct() }

        addTask(
            liveData = viewModel.vat,
            loadingMsg = "$syncType مالیات و عوارض ..."
        ) { viewModel.fetchVat() }

        addTask(
            liveData = viewModel.saleCenter,
            loadingMsg = "$syncType مراکز فروش ..."
        ) { viewModel.fetchSaleCenter() }

        addTask(
            liveData = viewModel.discount,
            loadingMsg = "$syncType تخفیفات ..."
        ) { viewModel.fetchDiscount() }

        // اولین کار را شروع کن
        runNextTask()
    }

    private fun <T> addTask(
        liveData: LiveData<NetworkResult<T>>,
        loadingMsg: String,
        fetchAction: () -> Unit
    ) {
        tasks.add {
            observeApiSequential(liveData, loadingMsg, fetchAction)
        }
    }

    private fun <T> observeApiSequential(
        liveData: LiveData<NetworkResult<T>>,
        loadingMsg: String,
        fetchAction: () -> Unit
    ) {
        liveData.observe(viewLifecycleOwner) { result ->
            when (result) {

                is NetworkResult.Loading -> {
                    showOrUpdateLoading(loadingMsg)
                }

                is NetworkResult.Success -> {

                    runNextTask()
                }

                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    runNextTask()
                }
            }
        }

        fetchAction()
    }

    private fun runNextTask() {
        if (currentTaskIndex < tasks.size) {
            tasks[currentTaskIndex].invoke()
            currentTaskIndex++
        } else {
            // همه کارها تمام شد
            loadingDialog.dismiss()

            onAllDataUpdatedSuccessfully()
        }
    }

    private fun onAllDataUpdatedSuccessfully() {
        lifecycleScope.launch {
            mainPreferences.setActUpdated()
            mainPreferences.setPatternUpdated()
            mainPreferences.setProductUpdated()
            mainPreferences.setDiscountUpdated()
        }
    }

    private fun openFactorScreen() {
        val action =
            HomeFragmentDirections.actionHomeFragmentToHeaderOrderFragment(
                typeCustomer = false,
                typeOrder = OrderType.Add.value,
                customerId = 0,
                customerName = ""
            )
        findNavController().navigate(action)
    }

    private fun initAdapter() {
        binding.rvHomeMenu.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.homeMenuItems.observe(viewLifecycleOwner) { items ->
            binding.rvHomeMenu.adapter = HomeMenuAdapter(items) { item ->
                when (item.id) {
                    1 -> { /* باز کردن صفحه کاتالوگ */
                        lifecycleScope.launch {
                            if (!isDatabaseReady()) {
                                showSyncRequiredMessage()
                                return@launch
                            }
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToProductListFragment(
                                    fromFactor = false
                                )
                            findNavController().navigate(action)
                        }
                    }

                    2 -> { /* باز کردن صفحه لیست کالاها */
                        lifecycleScope.launch {
                            if (!isDatabaseReady()) {
                                showSyncRequiredMessage()
                                return@launch
                            }
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToGroupProductFragment(
                                    fromFactor = false
                                )
                            findNavController().navigate(action)
                        }
                    }

                    3 -> { /* باز کردن صفحه ثبت سفارش */
                        lifecycleScope.launch {

                            if (!isDatabaseReady()) {
                                showSyncRequiredMessage()
                                return@launch
                            }

                            val canOpen = mainPreferences.hasDownloadedToday()
                            if (canOpen) {
                                openFactorScreen()
                            } else {
                                customDialogForceUpdate?.showDialog(
                                    activity,
                                    getString(R.string.error_receiving_product_pattern_act_mandatory),
                                    true,
                                    getString(R.string.label_no),
                                    getString(R.string.label_update),
                                    true,
                                    true
                                )
                            }
                        }
                    }

                    4 -> { /* باز کردن صفحه مشتریان */
                        lifecycleScope.launch {
                            if (!isDatabaseReady()) {
                                showSyncRequiredMessage()
                                return@launch
                            }
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToCustomerListFragment()
                            findNavController().navigate(action)
                        }
                    }

                    5 -> { /* باز کردن صفحه گزارشات */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToReportFragment()
                        findNavController().navigate(action)
                    }

                    6 -> { /* باز کردن صفحه سفارش‌ها */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToReportFactorFragment()
                        findNavController().navigate(action)
                    }

                    7 -> {/* خروج از حساب کاربری */
                        customDialog?.showDialog(
                            activity,
                            getString(R.string.msg_log_out),
                            true,
                            getString(R.string.label_no),
                            getString(R.string.label_ok),
                            true,
                            true
                        )
                    }

                    8 -> { /* باز کردن صفحه تنظیمات */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToSettingFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }


    private fun setupClicks() {
        binding.ivSync.setOnClickListener {
            lifecycleScope.launch {
                if (!isInternetAvailable(requireContext())) {
                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.error_network_internet),
                        SnackBarType.Error.value
                    )?.show()
                    return@launch
                }
                syncType = if (isDatabaseEmpty()) "در حال دریافت" else "در حال بروزرسانی"

                showOrUpdateLoading("$syncType")
                setupObserver()
            }
        }

        customDialog?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton { logout() }
        }

        customDialogForceUpdate?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton {
                lifecycleScope.launch {

                    if (!isInternetAvailable(requireContext())) {
                        CustomSnackBar.make(
                            requireView(),
                            getString(R.string.error_network_internet),
                            SnackBarType.Error.value
                        )?.show()
                        return@launch
                    }

                    try {
                        syncType = "در حال بروزرسانی"

                        val updateSteps = listOf(
                            "مصوبه" to suspend { viewModel.syncAct() },
                            "طرح فروش" to suspend { viewModel.syncPattern() },
                            "محصولات" to suspend { viewModel.syncProducts() },
                            "تخفیفات" to suspend { viewModel.syncDiscount() }
                        )

                        for ((title, action) in updateSteps) {
                            updateLoadingMessage("$syncType $title ...")
                            action()
                            delay(200)
                        }

                        loadingDialog.dismiss()
                    } catch (e: Exception) {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_updating_information),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }

    private fun initLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingBinding = DialogLoadingBinding.inflate(layoutInflater)

        loadingDialog.apply {
            setContentView(loadingBinding.root)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showOrUpdateLoading(message: String) {
        if (!::loadingDialog.isInitialized) initLoadingDialog()

        loadingBinding.tvLoadingMessage.text = message

        if (!loadingDialog.isShowing) {
            loadingDialog.show()
            loadingDialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.8).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        }
    }

    private suspend fun updateLoadingMessage(message: String) {
        withContext(kotlinx.coroutines.Dispatchers.Main) {
            showOrUpdateLoading(message)
        }
    }

    private fun handleBackToExit() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (doubleBackToExit) {
                requireActivity().finish()
            } else {
                doubleBackToExit = true
                CustomSnackBar.make(
                    requireView(),
                    getString(R.string.msg_press_back_button_again_exit),
                    SnackBarType.Warning.value
                )?.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExit = false
                }, 2000)
            }
        }

    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            // پاک کردن اطلاعات کاربر از DataStore
            mainPreferences.clearUserInfo()

            // پاک کردن تمام جدول‌ها
            db.applicationSettingDao().clearApplicationSetting() // جدول تنظیمات
            db.visitorDao().clearVisitors() // جدول ویزیتور
            db.groupProductDao().clearGroupProduct() // جدول گروه کالاها
            db.productDao().clearProducts() // جدول محصولات
            db.productImageDao().clearProductImage() // جدول تصاویر محصولات
            db.productPackingDao().clearProductPacking() // جدول بسته بندی محصولات
            db.customerDao().clearCustomers() // جدول مشتریان
            db.customerDirectionDao().clearCustomerDirection() // جدول مسیر مشتریان
            db.assignDirectionCustomerDao().clearAssignDirectionCustomer() // جدول مسیر مشتری
            db.invoiceCategoryDao().clearInvoiceCategory() // جدول گروه صورت حساب
            db.patternDao().clearPatterns() // جدول طرح فروش
            db.actDao().clearAct() // جدول مصوبه
            db.vatDao().clearVat() // جدول مالیات
            db.saleCenterDao().clearSaleCenters() // جدول مراکز فروش
            db.discountDao().clearDiscounts() // جدول تخفیف
            db.factorDao().clearFactorHeader() // جدول هدر فاکتور
            db.factorDao().clearFactorDetails() // جدول جزییات فاکتور
            db.factorDao().clearFactorDiscount() // جدول تخفیفات فاکتور
            db.factorDao().clearFactorGiftInfo() // جدول جایزه فاکتور

            // هدایت به صفحه Splash
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private suspend fun isDatabaseEmpty(): Boolean {
        return db.groupProductDao().getCount() == 0 ||
                db.productDao().getCount() == 0 ||
                db.customerDao().getCount() == 0
    }

    private suspend fun isDatabaseReady(): Boolean {
        return db.groupProductDao().getCount() > 0 &&
                db.productDao().getCount() > 0 &&
                db.customerDao().getCount() > 0
    }

    private fun showSyncRequiredMessage() {
        CustomSnackBar.make(
            requireView(),
            getString(R.string.error_click_sync_button_download_information),
            SnackBarType.Warning.value
        )?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // _loadingBinding = null
    }

}