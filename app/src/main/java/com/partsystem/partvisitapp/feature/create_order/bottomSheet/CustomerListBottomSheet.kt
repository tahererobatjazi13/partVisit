package com.partsystem.partvisitapp.feature.create_order.bottomSheet

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.BottomSheetCustomerListBinding
import com.partsystem.partvisitapp.feature.create_order.bottomSheet.adapter.CustomerBottomSheetAdapter
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
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
    lateinit var userPreferences: UserPreferences

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
        initAdapter()
        initRecyclerViews()
        observeData()
        setupSearch()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClicks() {
        binding.apply {
            binding.ivBack.setOnClickListener {
                dismiss()
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
        customerBottomSheetAdapter = CustomerBottomSheetAdapter(
            onClick = {
                val result = Bundle().apply {
                    putInt(ARG_ID, it.id)
                    putString(ARG_NAME, it.name)
                }
                parentFragmentManager.setFragmentResult(REQ_CLICK_ITEM, result)
                dismiss()
            }
        )
    }

    private fun initRecyclerViews() {
        binding.rvCustomer.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = customerBottomSheetAdapter
        }
    }

    private fun observeData() {
      /*  customerViewModel.customers.observe(viewLifecycleOwner) { filteredProducts ->
            if (filteredProducts.isEmpty()) {
                binding.info.show()
                binding.info.message(requireContext().getString(R.string.msg_no_customer))
                binding.rvCustomer.hide()
            } else {
                binding.info.gone()
                binding.rvCustomer.show()
                customerBottomSheetAdapter.setData(filteredProducts)
            }
        }*/
        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->

            if (customers.isEmpty()) {
                binding.info.show()
                binding.info.message(getString(R.string.msg_no_customer))
                binding.rvCustomer.hide()
            } else {
                binding.info.gone()
                binding.rvCustomer.show()
                customerBottomSheetAdapter.setData(customers)

//                // مقدار اول
//                val first = customers.first()
//                customerId = first.id
//                binding.tvCustomerName.text = first.name
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    companion object {
        const val REQ_CLICK_ITEM = "click_item_request"
        const val ARG_ID = "id"
        const val ARG_NAME = "name"
        fun newInstance(onDismiss: (() -> Unit)? = null) =
            CustomerListBottomSheet(onDismiss)
    }
}
