package com.partsystem.partvisitapp.feature.main.home.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.databinding.FragmentHomeBinding
import com.partsystem.partvisitapp.feature.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import ir.huri.jcal.JalaliCalendar
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
        initAdapter()
        rxBinding()
        //  setupObserver()
    }

    private fun init() {
        customDialog = CustomDialog.instance
        val jalaliDate = JalaliCalendar()
        binding.tvDate.text = "${jalaliDate.year}/${jalaliDate.month}/${jalaliDate.day}"

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

        viewModel.applicationSetting.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "تنظیمات با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchApplicationSetting()

        viewModel.groupProducts.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "گروه کالا ها با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchGroupProducts()

        viewModel.products.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "محصولات با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchProducts()

        viewModel.productImages.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "تصاویرکالا با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchProductImages()

        viewModel.productPacking.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "بسته بندی با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchProductPacking()

        viewModel.customers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "مشتریان با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchCustomers()

        viewModel.customerDirections.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "مسیر مشتریان با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchCustomerDirections()

        viewModel.invoiceCategory.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "گروه صورتحساب با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchInvoiceCategory()

        viewModel.pattern.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "طرح فروش با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchPattern()

        viewModel.act.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // show progress
                }

                is NetworkResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "مصوبه با موفقیت دریافت شد",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NetworkResult.Error -> {
                    Log.d("NetworkError6", result.message)

                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.fetchAct()

    }

    private fun initAdapter() {
        binding.rvHomeMenu.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.homeMenuItems.observe(viewLifecycleOwner) { items ->
            binding.rvHomeMenu.adapter = HomeMenuAdapter(items) { item ->
                when (item.id) {
                    1 -> { /* باز کردن صفحه کاتالوگ */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToProductListFragment(false)
                        findNavController().navigate(action)
                    }

                    2 -> { /* باز کردن صفحه لیست کالاها */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToGroupProductFragment(false)
                        findNavController().navigate(action)
                    }

                    3 -> { /* باز کردن صفحه ثبت سفارش */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHeaderOrderFragment()
                        findNavController().navigate(action)
                    }

                    4 -> { /* باز کردن صفحه مشتریان */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToCustomerListFragment()
                        findNavController().navigate(action)
                    }

                    5 -> { /* باز کردن صفحه گزارشات */
                    }

                    6 -> { /* باز کردن صفحه سفارش‌ها */
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToOnlineOrderListFragment()
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

    private fun rxBinding() {

        binding.ivSync.setOnClickListener {
            setupObserver()
        }
        customDialog?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton { logout() }
        }
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            // پاک کردن اطلاعات کاربر از DataStore
            userPreferences.clearUserInfo()

            // پاک کردن تمام جدول‌ها
            db.customerDao().clearAll()       // جدول مشتریان
            db.productDao().clearAll()        // جدول محصولات
            db.groupProductDao().clearAll()    // جدول گروه کالاها

            // هدایت کاربر به صفحه Splash
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}