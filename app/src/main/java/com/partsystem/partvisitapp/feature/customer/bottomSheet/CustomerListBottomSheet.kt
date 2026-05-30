package com.partsystem.partvisitapp.feature.customer.bottomSheet

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.BottomSheetCustomerListBinding
import com.partsystem.partvisitapp.feature.customer.bottomSheet.adapter.CustomerBottomSheetAdapter
import com.partsystem.partvisitapp.feature.create_order.ui.HeaderOrderFragment
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet.FilterOrderBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("UseCompatLoadingForDrawables")
class CustomerListBottomSheet(
    private val onDismissCallback: (() -> Unit)? = null
) : BottomSheetDialogFragment() {
    private val customerViewModel: CustomerViewModel by viewModels()

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: BottomSheetCustomerListBinding? = null
    private val binding get() = _binding!!
    private lateinit var customerBottomSheetAdapter: CustomerBottomSheetAdapter

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
        setupClearIcon()
        initAdapter()
        initRecyclerViews()
        observeData()
        setupSearch()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true

        val params = bottomSheet.layoutParams
        params.height = (resources.displayMetrics.heightPixels * 0.9).toInt()
        bottomSheet.layoutParams = params
    }

    private fun setupClicks() = binding.apply {
        ivBack.setOnClickListener {
            dismiss()
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

    private fun initAdapter() {
        customerBottomSheetAdapter = CustomerBottomSheetAdapter(
            onClick = { customer ->
                val result = Bundle().apply {
                    putInt(ARG_CUSTOMER_ID, customer.id)
                    putString(ARG_CUSTOMER_NAME, customer.name)
                }
                parentFragmentManager.setFragmentResult(REQ_CLICK_CUSTOMER, result)
                dismiss()
            }
        )
    }

    private fun initRecyclerViews() = binding.apply {
        rvList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = customerBottomSheetAdapter
        }
    }

    private fun observeData() = binding.apply {
        lifecycleScope.launch {
            val controlVisit = mainPreferences.controlVisitSchedule.first() ?: false
            val persianDate = getTodayPersianDateLatin()

            if (controlVisit) {
                //  با برنامه ویزیت
                customerViewModel.loadCustomersWithSchedule(persianDate)
            } else {
                //  بدون برنامه ویزیت
                customerViewModel.loadCustomersWithoutSchedule()
            }
        }

        rvList.hide()
        info.gone()

        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            if (customers.isEmpty()) {
                info.show()
                info.message(getString(R.string.msg_no_customer))
                rvList.hide()
            } else {
                info.gone()
                rvList.show()
                customerBottomSheetAdapter.setData(customers)
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
            customerViewModel.filterCustomers(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        (parentFragment as? FilterOrderBottomSheet)?.onBottomSheetDismissed()
        (parentFragment as? HeaderOrderFragment)?.onBottomSheetDismissed()
    }

    companion object {
        const val REQ_CLICK_CUSTOMER = "click_customer_request"
        const val ARG_CUSTOMER_ID = "customerId"
        const val ARG_CUSTOMER_NAME = "customerName"
        fun newInstance(onDismiss: (() -> Unit)? = null) =
            CustomerListBottomSheet(onDismiss)
    }
}
