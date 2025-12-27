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
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.databinding.DialogLoadingBinding
import com.partsystem.partvisitapp.databinding.FragmentHomeBinding
import com.partsystem.partvisitapp.feature.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

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
        //setupObserver()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {

        customDialog = CustomDialog.instance
        binding.tvDate.text = getTodayPersianDate()

        // جمع‌آوری دیتا از DataStore
        lifecycleScope.launch {
            combine(
                userPreferences.firstName,
                userPreferences.lastName
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
            loadingMsg = "در حال دریافت تنظیمات ..."
        ) { viewModel.fetchApplicationSetting() }

        addTask(
            liveData = viewModel.visitor,
            loadingMsg = "در حال دریافت ویزیتور ..."
        ) { viewModel.fetchVisitors() }
        addTask(
            liveData = viewModel.visitSchedule,
            loadingMsg = "در حال دریافت برنامه ویزیت ..."
        ) { viewModel.fetchVisitSchedules() }

        addTask(
            liveData = viewModel.groupProducts,
            loadingMsg = "در حال دریافت گروه کالا ..."
        ) { viewModel.fetchGroupProducts() }

        addTask(
            liveData = viewModel.products,
            loadingMsg = "در حال دریافت محصولات ..."
        ) { viewModel.fetchProducts() }

        addTask(
            liveData = viewModel.productImages,
            loadingMsg = "در حال دریافت تصاویر کالا ..."
        ) { viewModel.fetchProductImages() }

        addTask(
            liveData = viewModel.productPacking,
            loadingMsg = "در حال دریافت بسته‌بندی ..."
        ) { viewModel.fetchProductPacking() }

        addTask(
            liveData = viewModel.customers,
            loadingMsg = "در حال دریافت مشتریان ..."
        ) { viewModel.fetchCustomers() }

        addTask(
            liveData = viewModel.customerDirections,
            loadingMsg = "در حال دریافت مسیر مشتریان ..."
        ) { viewModel.fetchCustomerDirections() }

        addTask(
            liveData = viewModel.assignDirectionCustome,
            loadingMsg = "در حال دریافت مسیر مشتری ..."
        ) { viewModel.fetchAssignDirectionCustomer() }

        addTask(
            liveData = viewModel.invoiceCategory,
            loadingMsg = "در حال دریافت گروه صورتحساب ..."
        ) { viewModel.fetchInvoiceCategory() }

        addTask(
            liveData = viewModel.pattern,
            loadingMsg = "در حال دریافت طرح فروش ..."
        ) { viewModel.fetchPattern() }
        addTask(
            liveData = viewModel.patternDetails,
            loadingMsg = "در حال دریافت جزییات طرح فروش ..."
        ) { viewModel.fetchPatternDetails() }

        addTask(
            liveData = viewModel.act,
            loadingMsg = "در حال دریافت مصوبه ..."
        ) { viewModel.fetchAct() }

        addTask(
            liveData = viewModel.vat,
            loadingMsg = "در حال دریافت مالیات و عوارض ..."
        ) { viewModel.fetchVat() }

        addTask(
            liveData = viewModel.saleCenter,
            loadingMsg = "در حال دریافت مراکز فروش ..."
        ) { viewModel.fetchSaleCenter() }

        addTask(
            liveData = viewModel.discount,
            loadingMsg = "در حال دریافت تخفیفات ..."
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
        }
    }

    /*
        private fun setupObserver() {

            viewModel.applicationSetting.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت تنظیمات...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchApplicationSetting()

            viewModel.visitor.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت ویزیتور...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchVisitors()

            viewModel.groupProducts.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت گروه کالا...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchGroupProducts()

            viewModel.products.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت محصولات ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchProducts()

            viewModel.productImages.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت تصاویرکالا ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchProductImages()

            viewModel.productPacking.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت بسته بندی ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchProductPacking()

            viewModel.customers.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading("در حال دریافت مشتریان...")
                    }

                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchCustomers()

            viewModel.customerDirections.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت مسیر مشتریان ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchCustomerDirections()

            viewModel.assignDirectionCustome.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت مسیر مشتری ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchAssignDirectionCustomer()

            viewModel.invoiceCategory.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت گروه صورتحساب  ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchInvoiceCategory()

            viewModel.pattern.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت طرح فروش ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchPattern()

            viewModel.act.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت مصوبه ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchAct()

            viewModel.vat.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت مالیات و عوارض ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchVat()

            viewModel.saleCenter.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت مراکز فروش ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchSaleCenter()

            viewModel.discount.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Loading -> showLoading("در حال دریافت تخفیفات ...")
                    is NetworkResult.Success -> {
                        hideLoading()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            viewModel.fetchDiscount()
        }
    */

    private fun initAdapter() {
        binding.rvHomeMenu.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.homeMenuItems.observe(viewLifecycleOwner) { items ->
            binding.rvHomeMenu.adapter = HomeMenuAdapter(items) { item ->
                when (item.id) {
                    1 -> { /* باز کردن صفحه کاتالوگ */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToProductListFragment(
                                fromFactor = false
                            )
                        findNavController().navigate(action)
                    }

                    2 -> { /* باز کردن صفحه لیست کالاها */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToGroupProductFragment(
                                fromFactor = false
                            )
                        findNavController().navigate(action)
                    }

                    3 -> { /* باز کردن صفحه ثبت سفارش */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHeaderOrderFragment(
                                typeCustomer = false,
                                typeOrder = OrderType.Add.value,
                                customerId = 0,
                                customerName = ""
                            )
                        findNavController().navigate(action)
                    }

                    4 -> { /* باز کردن صفحه مشتریان */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToCustomerListFragment()
                        findNavController().navigate(action)
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
            setupObserver()
        }
        customDialog?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton { logout() }
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
        }
    }

    private fun hideLoadingIfDone() {
        loadingCount--

        if (loadingCount <= 0) {
            loadingCount = 0
            if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
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
            userPreferences.clearUserInfo()

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
            db.factorDao().clearFactorDetails() // جدول جزییلات فاکتور
            db.factorDao().clearFactorDiscount() // جدول تخفیفات فاکتور
            db.factorDao().clearFactorGiftInfo() // جدول جایزه فاکتور

            // هدایت به صفحه Splash
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // _loadingBinding = null
    }

}