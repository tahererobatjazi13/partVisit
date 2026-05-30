package com.partsystem.partvisitapp.feature.customer.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.FragmentCustomerListBinding
import com.partsystem.partvisitapp.feature.customer.dialog.AddEditCustomerDialog
import com.partsystem.partvisitapp.feature.customer.ui.adapter.CustomerListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class CustomerListFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentCustomerListBinding? = null
    private val binding get() = _binding!!

    private val customerViewModel: CustomerViewModel by viewModels()

    private lateinit var customerListAdapter: CustomerListAdapter
    private var customDialog: CustomDialog? = null

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupClearIcon()
        setupSearch()
        initAdapter()
        initRecyclerView()
        observeCustomers()
        loadCustomers()
        customDialog = CustomDialog()
    }

    private fun setupClicks() = binding.apply {
        hfCustomerList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        // مدیریت رویدادهای دیالوگ
        customDialog?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton {
                hideProgress()
            }
        }

        // دکمه افزودن مشتری
        fabAddCustomer.setOnClickListener {
            val dialog = AddEditCustomerDialog { _ ->
                // materialViewModel.insert(rawMaterial)
            }
            dialog.show(childFragmentManager, "AddRawCustomerDialog")
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

    private fun initAdapter() {
        customerListAdapter = CustomerListAdapter(
            onClick = { customer ->
                val action = CustomerListFragmentDirections
                    .actionCustomerListFragmentToCustomerDetailFragment(
                        customerId = customer.id
                    )
                findNavController().navigate(action)
            },
            onEdit = { customer ->
                val dialog = AddEditCustomerDialog(customer) { _ ->
                }
                dialog.show(childFragmentManager, "EditRawCustomerDialog")
            }
        )
    }

    private fun initRecyclerView() = with(binding.rvCustomer) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = customerListAdapter
    }

    private fun loadCustomers() {
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
    }

    private fun observeCustomers() {
        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            showCustomerList(customers)
        }
    }

    private fun showCustomerList(list: List<CustomerEntity>) = with(binding) {
        if (list.isEmpty()) {
            info.show()
            info.message(requireContext().getString(R.string.msg_no_customer))
            rvCustomer.hide()
        } else {
            info.gone()
            rvCustomer.show()
            customerListAdapter.setData(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}