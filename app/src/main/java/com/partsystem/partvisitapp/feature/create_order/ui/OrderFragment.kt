package com.partsystem.partvisitapp.feature.create_order.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.feature.create_order.adapter.OrderAdapter
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.OrderEntity
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderBinding
import com.partsystem.partvisitapp.feature.customer.ui.CustomerDetailFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private val factorViewModel: FactorViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    private val formatter = DecimalFormat("#,###,###,###")
    private var currentCartItems: List<FactorDetailEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        rxBinding()
        setupObserver()
        initAdapter()
    }
    private fun init() {
    }

    /**
     *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
     */
    private fun rxBinding() {
        binding.apply {
            hfOrder.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }
            tryAgain.setOnClickListener {
                // groupViewModel.fetchGroups(kind = 13)
                //tryAgain.gone()
            }
            btnDraftOrder.setOnClickListener {
                lifecycleScope.launch {
                 //   factorViewModel.saveToLocal()
                }

                val action =
                    OrderFragmentDirections.actionOrderFragmentToHomeFragment(
                    )
                findNavController().navigate(action)
            }

            btnSendOrder.setOnClickListener {
                //val json = factorViewModel.buildFinalJson()
                // api.sendFactor(json.toString())

                val action = OrderFragmentDirections.actionOrderFragmentToHomeFragment()
                findNavController().navigate(action)
            }
            btnCreateOrder.setOnClickBtnOneListener {

                if (currentCartItems.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_no_items_cart,
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickBtnOneListener
                } else {

                    val action =
                        OrderFragmentDirections.actionOrderFragmentToHomeFragment(
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupObserver() {

    }


    private fun initAdapter() {

        orderAdapter = OrderAdapter(loadProduct = { productId, actId ->
            factorViewModel.loadProduct(productId, actId ?: 195)
        },
            onQuantityChange = { item, quantity ->
                //  cartViewModel.updateQuantity(item.productId, quantity)
            },
            onDelete = { item ->
                factorViewModel.deleteFactorDetail(item)
                Log.d("DELETE_TEST", "delete productId = ${item.productId}")

            }
        )

        binding.rvOrder.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = orderAdapter
        }
//        val list = factorViewModel.selectedProducts
//        orderAdapter.submitList(list)
      /*  factorViewModel.selectedProducts.observe(viewLifecycleOwner) {
            orderAdapter.submitList(it.toList())    // مهم: toList ➜ create new list
        }
*/

        factorViewModel.details.observe(viewLifecycleOwner) { details ->
            // update UI list (در این نمونه ساده نمایش تعداد)
           // tvDetailsCount.text = "محصولات انتخاب‌شده: ${details.size}"
            orderAdapter.submitList(details)    // مهم: toList ➜ create new list

        }

/*        // فقط یک observe روی allFactorDetails
        factorViewModel.allFactorDetails.observe(viewLifecycleOwner) { list ->
            //  currentCartItems = list ?: emptyList()
            //  orderAdapter.submitList(list?.toList())

            orderAdapter.submitList(list)

            if (list.isNullOrEmpty()) {
                binding.info.show()
                binding.info.message(requireContext().getString(R.string.msg_no_data))
                binding.cvList.hide()
            } else {
                binding.info.gone()
                binding.cvList.show()
            }
            calculateTotalPrices(list)
        }*/
    }


    private fun calculateTotalPrices(items: List<FactorDetailEntity>?) {
        items ?: return
        val total = items.sumOf {
            it.price!!.toInt()
            /** it.unit1Value*/
        }
        with(binding) {
            tvTotalOrder.text = "${formatter.format(total)} ریال"
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