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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentCustomerListBinding
import com.partsystem.partvisitapp.feature.customer.dialog.AddEditCustomerDialog
import com.partsystem.partvisitapp.feature.customer.ui.adapter.CustomerListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerListFragment : Fragment() {

    private var _binding: FragmentCustomerListBinding? = null
    private val binding get() = _binding!!

    private lateinit var customerListAdapter: CustomerListAdapter
    private val customDialog by lazy { CustomDialog.instance }
    private val customerViewModel: CustomerViewModel by viewModels()

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
        initAdapter()
        initRecyclerViews()
        observeData()
        setupSearch()
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    private fun setupClicks() {
        binding.apply {

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

            etSearch.addTextChangedListener { editable ->
                val query = editable.toString()
                if (query.isNotEmpty()) {
                    // نمایش ضربدر و جستجو
                    binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        clearIcon,
                        null
                    )
                } else {
                    // فقط جستجو، بدون ضربدر
                    binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        searchIcon,
                        null
                    )
                }
            }

            // پاک کردن جستجو با لمس آیکون ضربدر
            etSearch.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = binding.etSearch.compoundDrawablesRelative[2] // drawableEnd
                    drawableEnd?.let {
                        val touchAreaStart =
                            binding.etSearch.width - binding.etSearch.paddingEnd - it.intrinsicWidth
                        if (event.rawX >= touchAreaStart) {
                            binding.etSearch.text?.clear()
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
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

    private fun initRecyclerViews() {
        binding.rvCustomer.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = customerListAdapter
        }
    }

    private fun observeData() {
        customerViewModel.customerList.observe(viewLifecycleOwner) { filteredProducts ->
            if (filteredProducts.isEmpty()) {
                binding.info.show()
                binding.info.message(requireContext().getString(R.string.msg_no_customer))
                binding.rvCustomer.hide()
            } else {
                binding.info.gone()
                binding.rvCustomer.show()
                customerListAdapter.setData(filteredProducts)
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString()
            customerViewModel.filterCustomers(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}