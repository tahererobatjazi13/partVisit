package com.partsystem.partvisitapp.feature.report_factor.online.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ReportFactorListType
import com.partsystem.partvisitapp.core.utils.ReportFactorVisitorType
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.extensions.showSyncRequiredMessage
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding
import com.partsystem.partvisitapp.feature.report_factor.online.adapter.OnlineOrderListAdapter
import com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet.FilterOrderBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class OnlineOrderListFragment : Fragment(), FilterOrderBottomSheet.OnFilterAppliedListener {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnlineOrderListViewModel by viewModels()

    private lateinit var onlineOrderListAdapter: OnlineOrderListAdapter
    private val args: OnlineOrderListFragmentArgs by navArgs()
    private var condition: String = ""
    private var isFilterBottomSheetOpen = false

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupClicks()
        setupClearIcon()
        initAdapter()
        setupObserver()
        setupSearch()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        lifecycleScope.launch {

            val savedCondition = mainPreferences.filterCondition.firstOrNull()
            val savedFromDate = mainPreferences.fromDate.firstOrNull()
            val savedToDate = mainPreferences.toDate.firstOrNull()
            val savedCustomerId = mainPreferences.customerId.firstOrNull() ?: 0
            // اضافه کردن خواندن کدهای مسیر
            val savedDirectionCodes = mainPreferences.directionCodes.firstOrNull()

            var activeFilters = 0

            // چک تاریخ شروع
            if (!savedFromDate.isNullOrBlank() && savedFromDate != getTodayPersianDate())
                activeFilters++

            // چک تاریخ پایان
            if (!savedToDate.isNullOrBlank() && savedToDate != getTodayPersianDate())
                activeFilters++

            // چک مشتری
            if (savedCustomerId != 0)
                activeFilters++

            // چک مسیر
            if (!savedDirectionCodes.isNullOrBlank())
                activeFilters++

            // تنظیم آیکن و نمایش تعداد فیلتر
            if (savedCondition.isNullOrBlank()) {
                binding.ivFilter.setImageDrawable(requireContext().getDrawable(R.drawable.ic_filter_account))
                binding.tvCountFilter.gone()
            } else {
                // در اینجا چک می‌کنیم که آیا شرطی وجود دارد یا خیر
                if (activeFilters != 0) {
                    binding.ivFilter.setImageDrawable(requireContext().getDrawable(R.drawable.ic_filter_account_red))
                    binding.tvCountFilter.show()
                    binding.tvCountFilter.text = "$activeFilters فیلتر"
                    condition = savedCondition
                } else {
                    // اگر شرط بود ولی activeFilters صفر بود (مثلاً حالتی که فقط AND خالی مانده)
                    condition = ""
                    binding.ivFilter.setImageDrawable(requireContext().getDrawable(R.drawable.ic_filter_account))
                    binding.tvCountFilter.gone()
                }
            }

            // ادامه کد مربوط به دریافت لیست...
            if (args.typeList == ReportFactorListType.Visitor.value) {
                if (args.typeVisitor == ReportFactorVisitorType.All.value) {
                    binding.hfOrderList.show()
                    binding.hfOrderList.textTitle =
                        requireContext().getString(R.string.label_order_list)
                    binding.clFilter.visibility = View.VISIBLE
                    viewModel.fetchReportFactorVisitorList(0, args.id, condition)
                    Log.d("conditionBase6", condition)

                } else {
                    val persianDate = getTodayPersianDateLatin()
                    condition = "And PersianDate = '$persianDate'"
                    viewModel.fetchReportFactorVisitorList(0, args.id, condition)
                    Log.d("conditionBase7", condition)

                    binding.hfOrderList.gone()
                    binding.clFilter.visibility = View.GONE
                }
            } else {
                binding.hfOrderList.show()
                viewModel.fetchReportFactorCustomerList(0, args.id)
            }
        }
    }

    override fun onFilterApplied() {
        init()
    }

    override fun onFilterCleared() {
        binding.ivFilter.setImageDrawable(requireContext().getDrawable(R.drawable.ic_filter_account))
        condition = ""
        binding.tvCountFilter.gone()
        viewModel.fetchReportFactorVisitorList(0, args.id, condition)
        Log.d("conditionBase2", condition)
    }

    private fun openFilterBottomSheet() {
        if (isFilterBottomSheetOpen) {
            return
        }
        isFilterBottomSheetOpen = true
        val bottomSheetFragment = FilterOrderBottomSheet()
        bottomSheetFragment.setOnFilterAppliedListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)

        bottomSheetFragment.dialog?.setOnDismissListener {
            isFilterBottomSheetOpen = false
        }
    }

    private fun setupClicks() = binding.apply {
        tryAgain.setOnClickListener {
            tryAgain.gone()
            if (args.typeList == ReportFactorListType.Visitor.value) {
                viewModel.fetchReportFactorVisitorList(0, args.id, condition)
                Log.d("conditionBase3", condition)
            } else {
                Log.d("conditionBase4", condition)
                viewModel.fetchReportFactorCustomerList(0, args.id)

            }
        }
        clFilter.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.checkDatabase { isReady ->
                    if (isReady) {
                        openFilterBottomSheet()
                    } else {
                        showSyncRequiredMessage()
                    }
                }
            }
        }

        hfOrderList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearIcon() = binding.apply {
        // پاک کردن جستجو با لمس آیکون ضربدر
        etSearch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd =
                    etSearch.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

                val touchAreaStart =
                    etSearch.width - etSearch.paddingEnd - drawableEnd.intrinsicWidth

                if (event.x >= touchAreaStart) {
                    etSearch.text?.clear()
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun initAdapter() = binding.apply {
        rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            onlineOrderListAdapter = OnlineOrderListAdapter(showSyncButton = false) { factors ->

                if (factors.finalPrice.toInt() != 0) {
                    val action =
                        OnlineOrderListFragmentDirections.actionOnlineOrderListFragmentToOnlineOrderDetailFragment(
                            factors.id
                        )
                    findNavController().navigate(action)
                } else {
                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.error_not_detail),
                        SnackBarType.Error.value
                    )?.show()
                }
            }
            adapter = onlineOrderListAdapter
        }
    }

    private fun setupObserver() = binding.apply {

        val liveData =
            if (args.typeList == ReportFactorListType.Visitor.value)
                viewModel.reportFactorVisitorList
            else
                viewModel.reportFactorCustomerList

        liveData.observe(viewLifecycleOwner) { result ->

            when (result) {

                is NetworkResult.Loading -> {
                    loading.show()
                    rvOrderList.hide()
                }

                is NetworkResult.Success -> {
                    loading.gone()

                    val orderList = result.data
                    if (orderList.isEmpty()) {
                        info.show()
                        info.message(getString(R.string.msg_no_order))
                        rvOrderList.hide()
                    } else {
                        info.gone()
                        rvOrderList.show()
                        onlineOrderListAdapter.submitList(orderList)
                    }
                }

                is NetworkResult.Error -> {
                    loading.gone()
                    tryAgain.show()
                    rvOrderList.hide()
                    tryAgain.message = result.message
                }
            }
        }
    }

    private fun setupSearch() = binding.apply {
        etSearch.addTextChangedListener { editable ->
            val query = convertNumbersToEnglish(fixPersianChars(editable.toString()))

            etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null,
                if (query.isEmpty()) searchIcon else clearIcon,
                null
            )

            if (args.typeList == ReportFactorListType.Visitor.value)
                viewModel.searchVisitorList(query)
            else
                viewModel.searchCustomerList(query)
        }
    }

    fun onBottomSheetDismissed() {
        isFilterBottomSheetOpen = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}