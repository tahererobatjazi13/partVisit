package com.partsystem.partvisitapp.feature.report_factor.ui.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.databinding.FragmentOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.adapter.OrderDetailAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.OrderType

@AndroidEntryPoint
class OfflineOrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private val formatter = DecimalFormat("#,###,###,###")
    private val args: OfflineOrderDetailFragmentArgs by navArgs()

    // private val viewModel: OrderListViewModel by viewModels()
    private val fakeOrders = mutableListOf<ReportFactorDto>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        rxBinding()
        // setupObserver()
        initAdapterFake()
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
                152000.0,
                1,
                "", 1,
                "ماست خامه ای", 2, "سطل",
                2.0, 1.0,
                2.0, 2, "بسته", 200.0, 1400.2,
                200.0, 100.0,
                2000.0, 2000.0
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
                412000.0,
                1,
                "", 1,
                "شیر لبنی", 2, "بسته",
                2.0, 1.0,
                200.0, 2, "کارتن", 200.0, 1400.2,
                200.0, 100.0,
                2000.0, 200.0
            )
        )


        binding.rvOrderDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            orderDetailAdapter = OrderDetailAdapter()
            adapter = orderDetailAdapter
            orderDetailAdapter.submitList(fakeOrders)

        }
    }


    private fun rxBinding() {
        binding.apply {
            hfOrderDetail.setOnClickImgTwoListener {
                binding.hfOrderDetail.gone()
                binding.svMain.gone()
                findNavController().navigateUp()
            }

            btnEditOrder.setOnClickListener {
                val bundle = bundleOf(
                    "typeCustomer" to true,
                    "typeOrder" to OrderType.Edit.value,
                    "customerId" to 0,
                    "customerName" to ""
                )

                val navController = requireActivity().findNavController(R.id.mainNavHost)
                navController.navigate(R.id.action_global_to_headerOrderFragment, bundle)
            }
        }
    }


    private fun initAdapter() {
        orderDetailAdapter = OrderDetailAdapter()
        binding.rvOrderDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderDetailAdapter
        }
        orderDetailAdapter.submitList(fakeOrders)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
