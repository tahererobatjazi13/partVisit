package com.partsystem.partvisitapp.feature.customer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.ReportFactorListType
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.databinding.FragmentCustomerDetailBinding
import com.partsystem.partvisitapp.feature.create_order.ui.HeaderOrderViewModel
import com.partsystem.partvisitapp.feature.customer.ui.adapter.CustomerDirectionAdapter

@AndroidEntryPoint
class CustomerDetailFragment : Fragment() {

    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!
    private val args: CustomerDetailFragmentArgs by navArgs()
    private val customerViewModel: CustomerViewModel by viewModels()
    private val headerOrderViewModel: HeaderOrderViewModel by viewModels()
    private lateinit var customerDirectionAdapter: CustomerDirectionAdapter
    private var customerName: String = ""
    private var customerId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setupClicks()
    }

    private fun observeData() {
        customerViewModel.getCustomerById(args.customerId).observe(viewLifecycleOwner) { customer ->
            if (customer != null) {
                customerName = customer.name
                customerId = customer.id
                binding.tvCustomerName.text = customer.name

                val details = listOfNotNull(customer.tafsiliPhone1?.takeIf { it.isNotBlank() },
                    customer.tafsiliPhone2?.takeIf { it.isNotBlank() },
                    customer.tafsiliMobile?.takeIf { it.isNotBlank() })

                if (details.isNotEmpty()) {
                    binding.tvPhone.text = details.joinToString(" | ")
                    binding.clCustomerPhone.show()
                } else {
                    binding.clCustomerPhone.gone()
                }
            }
        }


        headerOrderViewModel.getCustomerDirectionsByCustomer(args.customerId)
            .observe(viewLifecycleOwner) { directions ->
                val items = directions.mapNotNull { it.fullAddress }
                customerDirectionAdapter = CustomerDirectionAdapter(items)
                if (items.isNotEmpty()) {
                    binding.clCustomerAddress.show()
                    binding.rvAddress.adapter = customerDirectionAdapter
                    binding.rvAddress.layoutManager = LinearLayoutManager(requireContext())
                } else {
                    binding.clCustomerAddress.gone()
                }
            }

    }

    private fun setupClicks() {
        binding.apply {
            hfCustomerDetail.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            btnOrdersList.setOnClickListener {
                val action =
                    CustomerDetailFragmentDirections.actionCustomerDetailFragmentToOnlineOrderListFragment(
                        ReportFactorListType.Customer.value, args.customerId
                    )
                findNavController().navigate(action)
            }

            btRegisterOrder.setOnClickListener {
                val action =
                    CustomerDetailFragmentDirections.actionCustomerDetailFragmentToHeaderOrderFragment(
                        typeCustomer = true, typeOrder = OrderType.Add.value,
                        customerId = customerId, customerName = customerName
                    )
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
