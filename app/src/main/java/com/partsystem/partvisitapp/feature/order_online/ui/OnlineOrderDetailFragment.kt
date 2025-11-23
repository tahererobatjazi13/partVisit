package com.partsystem.partvisitapp.feature.order_online.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOnlineOrderDetailBinding
import com.partsystem.partvisitapp.feature.order_online.adapter.OrderDetailAdapter
import com.partsystem.partvisitapp.feature.order_online.model.OrderDetailFake
import java.text.DecimalFormat

@AndroidEntryPoint
class OnlineOrderDetailFragment : Fragment() {

    private var _binding: FragmentOnlineOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private val formatter = DecimalFormat("#,###,###,###")
    private val args: OnlineOrderDetailFragmentArgs by navArgs()
   // private val viewModel: OrderListViewModel by viewModels()

    val orders = listOf(
        OrderDetailFake(1, 1, "1", ""),
        OrderDetailFake(1, 1, "1", ""),
        OrderDetailFake(1, 1, "1", ""),
        OrderDetailFake(1, 1, "1", ""),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnlineOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        rxBinding()
       // setupObserver()

    }



    private fun rxBinding() {
        binding.apply {
            hfOrderDetail.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            tryAgain.setOnClickListener {
           //     viewModel.fetchOrderDetail(args.orderId)
                binding.tryAgain.gone()
            }
        }
    }


    private fun initAdapter() {
        orderDetailAdapter = OrderDetailAdapter()
        binding.rvOrder.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderDetailAdapter
        }
       orderDetailAdapter.submitList(orders)
    }


    // مشاهده‌ی تغییرات LiveData  جزییات یک سفارش
//    private fun setupObserver() {
//       // viewModel.fetchOrderDetail(args.orderId)
//
//        viewModel.orderDetail.observe(viewLifecycleOwner) { result ->
//            binding.apply {
//                when (result) {
//
//                    is NetworkResult.Loading -> {
//                        loading.show()
//                        svMain.gone()
//                    }
//
//                    is NetworkResult.Success -> {
//                        showOrderDetail(result.data)
//                    }
//
//                    is NetworkResult.Error -> {
//                        loading.gone()
//                        svMain.gone()
//                        tryAgain.show()
//                        tryAgain.message = result.message.toString()
//
//                    }
//
//                    else -> {
//                        loading.gone()
//                    }
//                }
//            }
//        }
//    }



    private fun showOrderDetail(order: OrderDetailFake?) = with(binding) {
        loading.gone()
        svMain.show()
        tryAgain.gone()

        order?.let {
            orderDetailAdapter.submitList(orders)
            calculateTotalPrices(orders)
        }
    }

    private fun calculateTotalPrices(items: List<OrderDetailFake>?) {
        items ?: return
        //  val total = items.sumOf { it.price }
        with(binding) {
            //  tvTotalOrder.text = "${formatter.format(total)} ریال"
            //  tvDiscountOrder.text = formatter.format(0) // جایگزین با مقدار واقعی
            //  tvTotalDiscount.text = formatter.format(0) // جایگزین با مقدار واقعی
            //  tvTotalPrice.text = formatter.format(total) + " ریال"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
