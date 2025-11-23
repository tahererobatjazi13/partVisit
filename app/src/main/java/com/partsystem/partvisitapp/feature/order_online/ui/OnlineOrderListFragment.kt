package com.partsystem.partvisitapp.feature.order_online.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.databinding.FragmentOnlineOrderListBinding
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.feature.order_online.adapter.OrderListAdapter
import com.partsystem.partvisitapp.feature.order_online.model.OrderFake


@AndroidEntryPoint
class OnlineOrderListFragment : Fragment() {

    private var _binding: FragmentOnlineOrderListBinding? = null
    private val binding get() = _binding!!
  //  private val viewModel: GroupViewModel by viewModels()

    private lateinit var orderListAdapter: OrderListAdapter
    private val fakeOrders = mutableListOf<OrderFake>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnlineOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // init()
        initAdapterFake()
        //  initAdapter()
        rxBinding()
        //setupObserver()
    }


    private fun initAdapterFake() {
        fakeOrders.clear()

        // داده‌های فیک
        fakeOrders.add(
            OrderFake(
                "علی رضایی",
                "فروشگاه الماس",
                "مشهد، توس",
                "09123456789",
                "مواد غذایی",
                "12345",
                "13/08/1404",
                "1,500,000 ریال", "1"
            )
        )
        fakeOrders.add(
            OrderFake(
                "زهرا احمدی",
                " سوپرمارکت خورشید",
                "مشهد، خیام",
                "09351234567",
                "سوپرمارکت",
                "12346",
                "12/08/1404",
                "1,000,000 ریال",  "2"
            )
        )


        fakeOrders.add(
            OrderFake(
                "ندا رضای",
                " هایپر آسمان",
                "مشهد، فرامرز",
                "0935120007",
                "هایپرمارکت",
                "125896",
                "12/08/1404",
                "500,000 ریال",  "1"
            )
        )
        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
            orderListAdapter = OrderListAdapter(fakeOrders)
            orderListAdapter = OrderListAdapter(
                fakeOrders,
                onClick = { order ->
                    val action = OnlineOrderListFragmentDirections
                        .actionOnlineOrderListFragmentToOnlineOrderDetailFragment(order.type)
                    findNavController().navigate(action)
                }
            )

            adapter = orderListAdapter
        }
    }

    private fun rxBinding() {
        /*    binding.tryAgain.setOnClickListener {
                viewModel.fetchGroups(kind = 13)
                binding.tryAgain.gone()
            }*/

        binding.hfOrders.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }
    /*    private fun initAdapter() {
           groupAdapter = GroupAdapter { group ->
               val detailsList = group.groupDetails.toList()

               groupDetailAdapter.submitList(detailsList) {
                   val isEmpty = detailsList.isEmpty()
                   if (isEmpty) {
                       binding.info.show()
                       binding.info.message(requireContext().getString(R.string.msg_no_data))
                       binding.rvGroupDetail.gone()
                   } else {
                       binding.info.gone()
                       binding.rvGroupDetail.show()
                   }
               }
           }

           binding.rvGroup.apply {
               layoutManager =
                   LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
               adapter = groupAdapter
           }
       }

       private fun updateGroupDetail(groupDetails: List<GroupDetail>) {
           groupDetailAdapter = GroupDetailAdapter { selectedGroupDetail ->
               val selectedGroup = groupAdapter.currentList[groupAdapter.selectedPosition]
               val action = GroupFragmentDirections
                   .actionGroupFragmentToCatalogListFragment(
                       selectedGroup.groupName,
                       selectedGroupDetail.groupDetailName,
                       selectedGroup.groupId,
                       selectedGroupDetail.groupDetailId
                   )
               findNavController().navigate(action)
           }

           binding.rvGroupDetail.apply {
               layoutManager = RtlGridLayoutManager(requireContext(), 3)
               adapter = groupDetailAdapter
               setHasFixedSize(true)
               isNestedScrollingEnabled = false
           }

           groupDetailAdapter.submitList(groupDetails)
       }

       /**
        *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
        */


      private fun setupObserver() {
           viewModel.fetchGroups(kind = 13) //  kind = 13 برای لیست سالن و میز

           viewModel.groups.observe(viewLifecycleOwner) { result ->
               binding.apply {
                   when (result) {
                       is NetworkResult.Loading -> {
                           loading.show()
                       }

                       is NetworkResult.Success -> {
                           loading.gone()

                           val groups = result.data.orEmpty()
                           if (groups.isEmpty()) {
                               info.show()
                               info.message(getString(R.string.msg_no_data))
                               rvGroup.gone()
                               rvGroupDetail.gone()
                           } else {
                               info.gone()
                               //  rvGroup.show()
                               groupAdapter.submitList(groups)
                               updateGroupDetail(groups[0].groupDetails)
                           }
                       }

                       is NetworkResult.Error -> {
                           loading.gone()
                           tryAgain.show()
                           tryAgain.message = result.message.toString()
                       }

                       else -> {
                           loading.gone()
                       }
                   }
               }
           }
       }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}