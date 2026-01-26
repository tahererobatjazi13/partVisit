package com.partsystem.partvisitapp.feature.report_factor.offline

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.report_factor.offline.adapter.OfflineOrderListAdapter
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class OfflineOrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var offlineOrderListAdapter: OfflineOrderListAdapter
    private var customDialogSend: CustomDialog? = null
    private var customDialogDelete: CustomDialog? = null
    private val factorViewModel: FactorViewModel by viewModels()

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }
    private var selectedFactor: FactorHeaderUiModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        initAdapter()
        observeData()
        setupClearIcon()
        setupSearch()
        customDialogSend = CustomDialog()
        customDialogDelete = CustomDialog()
    }

    private fun initAdapter() {
        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            offlineOrderListAdapter = OfflineOrderListAdapter(
                showSyncButton = true,/*factorViewModel,*/
                onDelete = { item ->
                    selectedFactor = item

                    customDialogDelete = CustomDialog().apply {

                        setOnClickNegativeButton {
                            selectedFactor = null
                            hideProgress()
                        }

                        setOnClickPositiveButton {
                            selectedFactor?.let {
                                factorViewModel.deleteFactor(it.factorId)
                            }
                            selectedFactor = null
                            hideProgress()
                        }
                    }

                    customDialogDelete?.showDialog(
                        requireActivity(),
                        getString(R.string.msg_sure_delete_orders),
                        true,
                        getString(R.string.label_no),
                        getString(R.string.label_ok),
                        true,
                        true
                    )
                }


            ) { factors ->
                if (factors.hasDetail) {
                    val action =
                        OfflineOrderListFragmentDirections.actionOfflineOrderListFragmentToOfflineOrderDetailFragment(
                            factors.factorId
                        )
                    findNavController().navigate(action)
                } else {
                    val bundle = bundleOf(
                        "typeCustomer" to true,
                        "typeOrder" to OrderType.Edit.value,
                        "customerId" to 0,
                        "customerName" to "",
                        "factorId" to factors.factorId
                    )
                    val navController = requireActivity().findNavController(R.id.mainNavHost)
                    navController.navigate(R.id.action_global_to_headerOrderFragment, bundle)
                }
            }

            adapter = offlineOrderListAdapter
        }
    }

    private fun observeData() {
        factorViewModel.filteredHeaders.observe(viewLifecycleOwner) { headers ->
            if (headers.isNullOrEmpty()) {
                binding.info.show()
                binding.info.message(getString(R.string.msg_no_data))
                binding.rvOrderList.hide()
                binding.btnSyncAllOrder.gone()
            } else {
                binding.info.gone()
                binding.rvOrderList.show()
                offlineOrderListAdapter.setData(headers)

                binding.btnSyncAllOrder.visibility =
                    if (headers.size > 2) View.VISIBLE else View.GONE
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupClicks() {
        binding.apply {

            btnSyncAllOrder.setOnClickBtnOneListener {
                customDialogSend?.showDialog(
                    activity,
                    getString(R.string.msg_sure_send_orders),
                    true,
                    getString(R.string.label_no),
                    getString(R.string.label_ok),
                    true,
                    true
                )
            }
        }

        customDialogDelete?.apply {
            customDialogDelete?.setOnClickNegativeButton {
                selectedFactor = null
                customDialogDelete?.hideProgress()
            }
            customDialogDelete?.setOnClickPositiveButton {
                selectedFactor?.let {
                    factorViewModel.deleteFactor(it.factorId)
                    selectedFactor = null
                }
                customDialogDelete?.hideProgress()
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearIcon() {
        binding.etSearch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd =
                    binding.etSearch.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

                val touchAreaStart =
                    binding.etSearch.width - binding.etSearch.paddingEnd - drawableEnd.intrinsicWidth

                if (event.x >= touchAreaStart) {
                    binding.etSearch.text?.clear()
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupSearch() {

        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString()

            binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                if (query.isEmpty()) searchIcon else clearIcon,
                null
            )
            binding.btnSyncAllOrder.visibility =
                if (query.isNullOrEmpty() && offlineOrderListAdapter.itemCount > 2)
                    View.VISIBLE
                else
                    View.GONE
            factorViewModel.filterHeaders(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}