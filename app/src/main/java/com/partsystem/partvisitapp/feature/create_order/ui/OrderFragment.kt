package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.OrderAdapter
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private val formatter = DecimalFormat("#,###,###,###")
    private var currentCartItems: List<FactorDetailUiModel> = emptyList()
    private val args: OrderFragmentArgs by navArgs()
    private var customDialog: CustomDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        initAdapter()
        setupObserver()
        observeSendFactor()
        customDialog = CustomDialog()

        if (args.factorId > 0) {
            binding.hfOrder.textTitle = getString(R.string.label_edit_order)

            if (args.sabt == 0) {
                binding.cbSabt.setChecked(false)
                binding.cbSabt.setEnabled(true)
            } else {
                binding.cbSabt.setChecked(false)
                binding.cbSabt.setEnabled(true)
            }
        } else {
            binding.hfOrder.textTitle = getString(R.string.label_register_order)
            binding.cbSabt.setChecked(false)
            binding.cbSabt.setEnabled(true)
        }

        if (binding.cbSabt.isChecked) {

            factorViewModel.updateHeader(sabt = 1)

            lifecycleScope.launch {
                val updatedHeader =
                    factorViewModel.factorHeader.value?.copy(sabt = 1)
                updatedHeader?.let {
                    factorViewModel.updateFactorHeader(it)
                }
            }
        } else {
            factorViewModel.updateHeader(sabt = 0)

            lifecycleScope.launch {
                val updatedHeader =
                    factorViewModel.factorHeader.value?.copy(sabt = 0)
                updatedHeader?.let {
                    factorViewModel.updateFactorHeader(it)
                }
            }
        }
    }

    /**
     *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
     */
    private fun setupClicks() {
        binding.apply {
            hfOrder.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            bmbSendOrder.setOnClickBtnOneListener {
                if (currentCartItems.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_no_row_for_order,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickBtnOneListener
                }

                if (binding.cbSabt.isChecked) {
                    // تکمیل سفارش → ارسال به سرور
                    factorViewModel.sendFactor(
                        factorId = args.factorId,
                        sabt = 1
                    )
                } else {
                    // تیک نزده → هشدار
                    showWarningDialog()
                }
            }
            cbSabt.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        factorViewModel.calculateDiscountInsert(
                            applyKind = DiscountApplyKind.FactorLevel.ordinal,
                            factorHeader = factorViewModel.factorHeader.value ?: return@launch,
                            factorDetail = null
                        )
                    }
                } else {
                    factorViewModel.removeGiftsAndDiscounts(factorViewModel.factorHeader.value.id)
                }
            }
        }
    }

    private fun showWarningDialog() {
        customDialog = CustomDialog().apply {

            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton {
                factorViewModel.resetHeader()
                factorViewModel.enteredProductPage = false
                navigateToReportFactor()
                hideProgress()
            }
        }

        customDialog?.showDialog(
            activity,
            getString(R.string.error_order_not_completed),
            getString(R.string.error_save_order_draft),
            true,
            getString(R.string.label_close),
            getString(R.string.label_confirm),
            true,
            true
        )
    }


    private fun navigateToReportFactor() {
        val navController = findNavController()
        navController.navigate(
            R.id.reportFactorFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build()
        )
    }

    private fun observeSendFactor() {

        factorViewModel.sendFactorResult.observe(viewLifecycleOwner) { event ->

            event.getContentIfNotHandled()?.let { result ->

                when (result) {

                    is NetworkResult.Loading -> binding.bmbSendOrder.checkShowPbOne(true)

                    is NetworkResult.Success -> {
                        binding.bmbSendOrder.checkShowPbOne(false)

                        val message =
                            result.message ?: getString(R.string.msg_order_successfully_sent)

                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHomeClearOrder()
                    }

                    is NetworkResult.Error -> {
                        binding.bmbSendOrder.checkShowPbOne(false)

                        CustomSnackBar.make(
                            requireActivity().findViewById(android.R.id.content),
                            result.message,
                            SnackBarType.Error.value
                        )?.show()

                    }
                }
            }
        }

    }

    private fun initAdapter() {
        orderAdapter = OrderAdapter(
            onDelete = { item ->
                factorViewModel.deleteFactorDetail(item)
            }
        )

        binding.rvOrder.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = orderAdapter
        }
    }

    private fun setupObserver() {
        factorViewModel.getFactorDetailUi(factorId = args.factorId)
            .observe(viewLifecycleOwner) { details ->
                currentCartItems = details ?: emptyList()

                if (details.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(requireContext().getString(R.string.msg_no_data))
                    binding.svMain.hide()
                } else {
                    binding.info.gone()
                    binding.svMain.show()
                }
                orderAdapter.submitList(details)

                calculateTotalPrices(details)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrices(items: List<FactorDetailUiModel>?) {
        items ?: return
        val sumPrice = items.sumOf {
            it.unit1Rate * it.unit1Value
        }
        val sumDiscountPrice = items.sumOf {
            it.discountPrice
        }
        val sumVat = items.sumOf {
            it.vat
        }
        with(binding) {
            tvSumPrice.text = "${formatter.format(sumPrice)} ریال"
            tvSumDiscountPrice.text = "${"-" + formatter.format(sumDiscountPrice)} ریال"
            tvSumVat.text = "${formatter.format(sumVat)} ریال"
            tvFinalPrice.text = "${formatter.format((sumPrice - sumDiscountPrice) + sumVat)} ریال"
        }
        factorViewModel.updateHeader(finalPrice = (sumPrice - sumDiscountPrice) + sumVat)

        lifecycleScope.launch {
            val updatedHeader =
                factorViewModel.factorHeader.value?.copy(finalPrice = (sumPrice - sumDiscountPrice) + sumVat)
            updatedHeader?.let {
                factorViewModel.updateFactorHeader(it)
            }
        }
    }

    private fun navigateToHomeClearOrder() {
        val navController = findNavController()
        navController.navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}