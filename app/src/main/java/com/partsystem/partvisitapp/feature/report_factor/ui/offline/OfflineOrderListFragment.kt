package com.partsystem.partvisitapp.feature.report_factor.ui.offline

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.feature.report_factor.adapter.OrderListAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding

@AndroidEntryPoint
class OfflineOrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderListAdapter: OrderListAdapter
    private val fakeOrders = mutableListOf<ReportFactorDto>()
    private var customDialog: CustomDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapterFake()
        setupClicks()
        customDialog = CustomDialog.instance
        binding.btnSyncAllOrder.show()
    }

    private fun initAdapterFake() {
        fakeOrders.clear()

        // داده‌های فیک
        fakeOrders.add(
            ReportFactorDto(
                1,
                2,
                3,
                "13/08/1404",
                "07:25",
                1,
                "", 1,
                "زهرا احمدی", 2, "طرح فروش",
                12000.0,
                120.0,
                123.0,
                152.0,
                1,
                "", 1,
                "زهرا احمدی", 2, "",
                2.0, 1.0,
                2.0, 2, "", 2.0, 14.2,
                2.0, 1.0,
                2.0, 2.0
            )
        )
        fakeOrders.add(
            ReportFactorDto(
                2,
                2,
                3,
                "13/08/1404",
                "07:25",
                1,
                "", 1,
                "احمد رسولی", 2, "طرح فروش",
                12000.0,
                120.0,
                123.0,
                152.0,
                1,
                "", 1,
                "زهرا احمدی", 2, "",
                2.0, 1.0,
                2.0, 2, "", 2.0, 14.2,
                2.0, 1.0,
                2.0, 2.0
            )
        )


        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            orderListAdapter = OrderListAdapter(showSyncButton = true) { factors ->
                val action =
                    OfflineOrderListFragmentDirections.actionOfflineOrderListFragmentToOfflineOrderDetailFragment(
                        factors.id
                    )
                findNavController().navigate(action)
            }
            adapter = orderListAdapter
            orderListAdapter.submitList(fakeOrders)

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