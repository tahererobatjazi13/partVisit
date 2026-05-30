package com.partsystem.partvisitapp.feature.report_factor.offline

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.partsystem.partvisitapp.feature.create_order.ui.HeaderOrderViewModel
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.offline.adapter.OfflineOrderDetailAdapter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OfflineOrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val args: OfflineOrderDetailFragmentArgs by navArgs()

    private val factorViewModel: FactorViewModel by viewModels()
    private val headerOrderViewModel: HeaderOrderViewModel by viewModels()
    private val customerViewModel: CustomerViewModel by viewModels()

    private lateinit var offlineOrderDetailAdapter: OfflineOrderDetailAdapter
    private val formatter = DecimalFormat("#,###,###,###")

    private var currentSabt = 0
    private var hasDetails = false
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
        initRecyclerView()
        setupClicks()
        observeData()
        binding.svMain.gone()
    }

    private fun initRecyclerView() {
        offlineOrderDetailAdapter = OfflineOrderDetailAdapter()

        binding.rvOrderDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = offlineOrderDetailAdapter
        }
    }

    private fun setupClicks() = binding.apply {

        hfOrderDetail.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        btnEditOrder.setOnClickListener {
            handleEditOrderClick()
        }
    }

    private fun handleEditOrderClick() {

        when {
            currentSabt == 1 -> navigateToOrderFragment() // فاکتور تکمیل شده → صفحه سفارشات

            !hasDetails -> navigateToHeaderOrderFragment()

            else -> navigateToProducts()
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

        requireActivity()
            .findNavController(R.id.mainNavHost)
            .navigate(R.id.action_global_to_orderFragment, bundle)

    }

    // ناوبری به صفحه محصولات/هدر (برای فاکتورهای ناتمام)
    private fun navigateToHeaderOrderFragment() {

        val bundle = bundleOf(
            "typeCustomer" to true,
            "typeOrder" to OrderType.Edit.value,
            "customerId" to 0,
            "customerName" to "",
            "factorId" to args.factorId
        )

        requireActivity()
            .findNavController(R.id.mainNavHost)
            .navigate(R.id.action_global_to_headerOrderFragment, bundle)
    }

    private fun navigateToProducts() {
        val bundle = bundleOf(
            "fromFactor" to true,
            "actId" to args.actId,
            "typeOrder" to OrderType.Edit.value,
            "factorId" to args.factorId
        )

        val destination =
            if (productSelectionType == "group") {
                R.id.action_global_to_groupProductFragment
            } else {
                R.id.action_global_to_productListFragment
            }

        requireActivity()
            .findNavController(R.id.mainNavHost)
            .navigate(destination, bundle)
    }


    private fun observeData() {

        observeHeader()

        observeDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun observeHeader() {

        factorViewModel.getHeaderById(args.factorId)
            .observe(viewLifecycleOwner) { header ->
                with(binding) {

                    tvOrderNumber.text = args.factorId.toString()
                    tvDateTime.text =
                        gregorianToPersian(header.createDate.toString())
                }
                currentSabt = header.sabt
                productSelectionType = header.productSelectionType

                loadCustomerName(header.customerId)
                loadPatternName(header.patternId)
            }
    }

    private fun loadCustomerName(customerId: Int?) {
        // دریافت نام مشتری بر اساس ID
        customerId ?: return

        customerViewModel.getCustomerById(customerId)
            .observe(viewLifecycleOwner) { customer ->

                binding.tvCustomerName.text = customer.name
            }
    }
    private fun loadPatternName(patternName: Int?) {
        // دریافت نام طرح فروش بر اساس ID
        patternName ?: return

        headerOrderViewModel.getPatternById(patternName)
            .observe(viewLifecycleOwner) { pattern ->

                binding.tvPatternName.text = pattern.name
            }
    }

    private fun observeDetails() {

        factorViewModel.getFactorDetailUi(args.factorId)
            .observe(viewLifecycleOwner) { details ->

                hasDetails = details.isNotEmpty()

                updateEmptyState(details)

                offlineOrderDetailAdapter.submitList(details)

                calculateTotals(details)
            }
    }

    private fun updateEmptyState(details: List<FactorDetailUiModel>) = with(binding) {

        if (details.isEmpty()) {

            info.show()
            info.message(getString(R.string.msg_no_data))

            svMain.hide()

        } else {

            info.gone()
            svMain.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotals(items: List<FactorDetailUiModel>) {

        // محاسبه قیمت کل و مالیات
        val totalPrice = items.sumOf {
            it.unit1Rate * it.unit1Value
        }

        val totalVat = items.sumOf {
            it.vat
        }

        //  دریافت تخفیف کل (سطوح ردیف + فاکتور)
        lifecycleScope.launch {
            val totalDiscount = factorViewModel.getTotalDiscountForFactor(args.factorId)
            //  محاسبه مبلغ نهایی
            val finalPrice =
                (totalPrice - totalDiscount) + totalVat


            // آپدیت UI
            with(binding) {
                tvSumPrice.text = "${formatter.format(totalPrice)} ریال"
                tvSumDiscountPrice.text = "${"-" + formatter.format(totalDiscount)} ریال"
                tvSumVat.text = "${formatter.format(totalVat)} ریال"
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

