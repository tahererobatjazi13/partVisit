package com.partsystem.partvisitapp.feature.report_factor.ui.offline

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.create_order.ui.HeaderOrderViewModel
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.adapter.OfflineOrderListAdapter

@AndroidEntryPoint
class OfflineOrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var offlineOrderListAdapter: OfflineOrderListAdapter
    private var customDialog: CustomDialog? = null
    private val factorViewModel: FactorViewModel by viewModels()
    private val customerViewModel: CustomerViewModel by viewModels()
    private val headerOrderViewModel: HeaderOrderViewModel by viewModels()

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
        customDialog = CustomDialog.instance
        binding.btnSyncAllOrder.show()
    }

    private fun initAdapter() {
        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            offlineOrderListAdapter = OfflineOrderListAdapter(
                showSyncButton = true, customerViewModel,
                headerOrderViewModel
            ) { factors ->
                if (factors.hasDetail) {
                    val action =
                        OfflineOrderListFragmentDirections.actionOfflineOrderListFragmentToOfflineOrderDetailFragment(
                            factors.id
                        )
                    findNavController().navigate(action)
                } else {
                    val bundle = bundleOf(
                        "typeCustomer" to true,
                        "typeOrder" to OrderType.Edit.value,
                        "customerId" to 0,
                        "customerName" to "",
                        "factorId" to factors.id
                    )
                    val navController = requireActivity().findNavController(R.id.mainNavHost)
                    navController.navigate(R.id.action_global_to_headerOrderFragment, bundle)
                }
            }
            adapter = offlineOrderListAdapter
        }
    }

    private fun observeData() {
        factorViewModel.allHeaders.observe(viewLifecycleOwner) { headers ->
            if (headers.isNullOrEmpty()) {
                binding.info.show()
                binding.info.message(getString(R.string.msg_no_data))
                binding.rvOrderList.hide()
            } else {
                binding.info.gone()
                binding.rvOrderList.show()
                offlineOrderListAdapter.setData(headers)
            }
        }
    }

    private fun setupClicks() {
        binding.btnSyncAllOrder.setOnClickBtnOneListener {
            customDialog?.showDialog(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}