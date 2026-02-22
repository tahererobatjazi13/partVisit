package com.partsystem.partvisitapp.feature.report_factor.offline

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.databinding.FragmentOrderDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.extensions.gregorianToPersian
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.offline.adapter.OfflineOrderDetailAdapter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OfflineOrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var offlineOrderDetailAdapter: OfflineOrderDetailAdapter
    private val formatter = DecimalFormat("#,###,###,###")
    private val args: OfflineOrderDetailFragmentArgs by navArgs()
    private val factorViewModel: FactorViewModel by viewModels()
    private val customerViewModel: CustomerViewModel by viewModels()
    private var currentSabt: Int = 0
    private var hasDetails: Boolean = false
    private var productSelectionType = ""

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
        setupObserver()
        rxBinding()
        binding.svMain.gone()
    }

    private fun rxBinding() {
        binding.apply {
            hfOrderDetail.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            btnEditOrder.setOnClickListener {
                when (currentSabt) {
                    1 -> navigateToOrderFragment()    // فاکتور تکمیل شده → صفحه سفارشات
                    else -> if (!hasDetails) {
                        navigateToHeaderProducts()
                    } else {
                        navigateToProducts()
                    }
                }
            }
        }
    }

    // ناوبری به صفحه سفارشات (برای فاکتورهای کامل‌شده)
    private fun navigateToOrderFragment() {
        val bundle = bundleOf(
            "typeOrder" to OrderType.Edit.value,
            "factorId" to args.factorId,
            "sabt" to currentSabt,
            "isEditingCompletedOrder" to true
        )
        val navController = requireActivity().findNavController(R.id.mainNavHost)
        navController.navigate(R.id.action_global_to_orderFragment, bundle)
    }

    // ناوبری به صفحه محصولات/هدر (برای فاکتورهای ناتمام)
    private fun navigateToHeaderProducts() {
        val bundle = bundleOf(
            "typeCustomer" to true,
            "typeOrder" to OrderType.Edit.value,
            "customerId" to 0,
            "customerName" to "",
            "factorId" to args.factorId
        )

        val navController = requireActivity().findNavController(R.id.mainNavHost)
        navController.navigate(R.id.action_global_to_headerOrderFragment, bundle)
    }

    private fun navigateToProducts() {
        if (productSelectionType == "group"
        ) {
            val bundle = bundleOf(
                "fromFactor" to true,
                "actId" to args.actId,
                "typeOrder" to OrderType.Edit.value,
                "factorId" to args.factorId
            )

            requireActivity()
                .findNavController(R.id.mainNavHost)
                .navigate(R.id.action_global_to_groupProductFragment, bundle)
        } else {
            val bundle = bundleOf(
                "fromFactor" to true,
                "actId" to args.actId,
                "typeOrder" to OrderType.Edit.value,
                "factorId" to args.factorId
            )

            requireActivity()
                .findNavController(R.id.mainNavHost)
                .navigate(R.id.action_global_to_productListFragment, bundle)
        }

    }

    private fun initAdapter() {
        binding.rvOrderDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            offlineOrderDetailAdapter = OfflineOrderDetailAdapter()
            adapter = offlineOrderDetailAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObserver() {
        factorViewModel.getHeaderById(args.factorId)
            .observe(viewLifecycleOwner) { header ->
                binding.tvOrderNumber.text = args.factorId.toString()
                // دریافت نام مشتری بر اساس ID
                customerViewModel.getCustomerById(header.customerId!!)
                    .observe(viewLifecycleOwner) { customer ->
                        binding.tvCustomerName.text = customer.name
                    }
                binding.tvDateTime.text = gregorianToPersian(header.createDate.toString())
                currentSabt = header.sabt
                productSelectionType = header.productSelectionType
                // نمایش وضعیت فاکتور در UI
            }

        factorViewModel.getFactorDetailUi(factorId = args.factorId)
            .observe(viewLifecycleOwner) { details ->
                hasDetails = !details.isNullOrEmpty()

                if (details.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(requireContext().getString(R.string.msg_no_data))
                    binding.svMain.hide()
                } else {
                    binding.info.gone()
                    binding.svMain.show()
                }
                offlineOrderDetailAdapter.submitList(details)
                calculateTotalPrices(details)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrices(items: List<FactorDetailUiModel>?) {
        items ?: return

        // محاسبه قیمت کل و مالیات
        val sumPrice = items.sumOf { it.unit1Rate * it.unit1Value }
        val sumVat = items.sumOf { it.vat }

        //  دریافت تخفیف کل (سطوح ردیف + فاکتور)
        lifecycleScope.launch {
            val totalDiscount = factorViewModel.getTotalDiscountForFactor(args.factorId)

            //  محاسبه مبلغ نهایی
            val finalPrice = (sumPrice - totalDiscount) + sumVat

            // آپدیت UI
            with(binding) {
                tvSumPrice.text = "${formatter.format(sumPrice)} ریال"
                tvSumDiscountPrice.text = "${"-" + formatter.format(totalDiscount)} ریال"
                tvSumVat.text = "${formatter.format(sumVat)} ریال"
                tvFinalPrice.text = "${formatter.format(finalPrice)} ریال"
            }

            // ذخیره مبلغ نهایی در هدر
            factorViewModel.updateHeader(finalPrice = finalPrice)
            factorViewModel.factorHeader.value?.let { header ->
                lifecycleScope.launch {
                    factorViewModel.updateFactorHeader(header.copy(finalPrice = finalPrice))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

