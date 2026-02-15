package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
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
    private var backCallback: OnBackPressedCallback? = null
    private var isEditingCompletedOrder = false //  فلگ برای تشخیص حالت ویرایش

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackNavigationRestriction()
        isEditingCompletedOrder = args.isEditingCompletedOrder

        setupClicks()
        initAdapter()
        setupObserver()
        observeSendFactor()
        customDialog = CustomDialog()

        if (args.factorId > 0) {
            binding.hfOrder.textTitle = getString(R.string.label_edit_order)
            binding.cbSabt.isChecked = (args.sabt == 1)
            binding.cbSabt.isEnabled = true

            // 🔑 تنظیم وضعیت اولیه آداپتر بر اساس مقدار ذخیره‌شده
            orderAdapter.setOrderCompleted(args.sabt == 1)
        } else {
            binding.hfOrder.textTitle = getString(R.string.label_register_order)
            binding.cbSabt.isChecked = false
            binding.cbSabt.isEnabled = true

            // 🔑 وضعیت پیش‌فرض: سفارش تکمیل نشده
            orderAdapter.setOrderCompleted(false)
        }

    }

    /**
     * راه‌اندازی یکپارچه مدیریت بازگشت (هم سخت‌افزاری و هم هدر)
     * ⚠️ این متد حتماً باید در ابتدای onViewCreated فراخوانی شود
     */
    private fun setupBackNavigationRestriction() {
        // مدیریت دکمه بازگشت سخت‌افزاری - همیشه فعال باشد
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressAttempt()
            }
        }.also {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }

        // مدیریت دکمه بازگشت هدر - همان رفتار سخت‌افزاری
        binding.hfOrder.setOnClickImgTwoListener {
            handleBackPressAttempt()
        }
    }

    /**
     * منطق هوشمند بازگشت:
     * - اگر در حالت ویرایش سفارش تکمیل‌شده هستیم → اجازه بازگشت به صفحه جزئیات
     * - اگر در حالت عادی و تیک تکمیل زده شده → نمایش هشدار
     * - در غیر این صورت → بازگشت عادی
     */
    private fun handleBackPressAttempt() {
        // حالت ویرایش سفارش تکمیل ‌شده: همیشه اجازه بازگشت به صفحه جزئیات
        if (isEditingCompletedOrder) {
            findNavController().navigateUp()
            return
        }

        //  حالت عادی + تیک تکمیل زده شده: مسدود کردن بازگشت
        if (binding.cbSabt.isChecked) {
            CustomDialog().apply {
                setOnClickNegativeButton { hideProgress() }
                setOnClickPositiveButton { hideProgress() }
            }.showDialog(
                requireActivity(),
                getString(R.string.label_attention),
                getString(R.string.msg_cannot_go_back_warning),
                false,
                getString(R.string.label_understand),
                null,
                false,
                true
            )
            return
        }

        // بازگشت عادی
        findNavController().navigateUp()
    }

    /**
     * فعال‌سازی بازگشت (وقتی تیک تکمیل زده نشده)
     */
    private fun enableBackNavigation() {
        backCallback?.isEnabled = true
        binding.hfOrder.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    /**
     * غیرفعال‌سازی بازگشت (وقتی تیک تکمیل زده شده)
     */
    private fun disableBackNavigation() {
        backCallback?.isEnabled = false
        binding.hfOrder.setOnClickImgTwoListener {
            // نمایش پیام توضیحی
            CustomDialog().apply {
                setOnClickNegativeButton { hideProgress() }
                setOnClickPositiveButton { hideProgress() }
            }.showDialog(
                requireActivity(),
                getString(R.string.label_attention),
                getString(R.string.msg_cannot_go_back_warning),
                false,
                getString(R.string.label_understand),
                null,
                false,
                true
            )
        }
    }

    /**
     *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
     */
    private fun setupClicks() {
        binding.apply {

            btnDraftOrder.setOnClickListener {
                factorViewModel.resetHeader()
                factorViewModel.enteredProductPage = false
                navigateToReportFactor()
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
                    calculateTotalPrices(currentCartItems)

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
                // 🔑 اولویت اول: آپدیت رابط کاربری
                orderAdapter.setOrderCompleted(isChecked)

                if (isChecked) {
                    // اعمال تخفیف سطح فاکتور
                    if (args.sabt == 0 || factorViewModel.discountManuallyRemoved.value == true) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            factorViewModel.calculateDiscountInsert(
                                applyKind = DiscountApplyKind.FactorLevel.ordinal,
                                factorHeader = factorViewModel.factorHeader.value ?: return@launch,
                                factorDetail = null
                            )
                            factorViewModel.markDiscountApplied()
                            // ✅ پس از اعمال تخفیف، مبالغ را بازحساب کن
                            calculateTotalPrices(currentCartItems)
                        }
                    }
                    // آپدیت وضعیت sabt در حافظه
                    factorViewModel.updateHeader(sabt = 1)
                } else {
                    // حذف تخفیف‌ها و به‌روزرسانی هدر در یک تراکنش
                    viewLifecycleOwner.lifecycleScope.launch {
                        // 1. حذف تخفیف‌های سطح فاکتور و هدایا
                        factorViewModel.removeGiftsAndDiscounts(args.factorId)
                        factorViewModel.markDiscountRemoved()

                        // 2. آپدیت وضعیت sabt در حافظه
                        factorViewModel.updateHeader(sabt = 0)

                        // 3. ✅ مهم: کمی تأخیر برای اطمینان از اتمام حذف تخفیف‌ها
                      //  delay(100)

                        // 4. بازحساب مبالغ و ذخیره در دیتابیس
                        calculateTotalPrices(currentCartItems)

                        // 5. آپدیت نهایی sabt در دیتابیس
                        factorViewModel.factorHeader.value?.let { header ->
                            factorViewModel.updateFactorHeader(header.copy(sabt = 0))
                        }
                    }
                }
            }        }
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
                if (binding.cbSabt.isChecked) {
                    CustomDialog().apply {
                        setOnClickNegativeButton { hideProgress() }
                        setOnClickPositiveButton { hideProgress() }
                    }.showDialog(
                        requireActivity(),
                        getString(R.string.label_attention),
                        getString(R.string.msg_cannot_delete_when_completed),
                        false,
                        getString(R.string.label_understand),
                        null,
                        false,
                        true
                    )
                    return@OrderAdapter
                }
                // حذف
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

        // 1. محاسبه پایه (قیمت کل و مالیات)
        val sumPrice = items.sumOf { it.unit1Rate * it.unit1Value }
        val sumVat = items.sumOf { it.vat }

        // 2. دریافت تخفیف کل و به‌روزرسانی هدر
        lifecycleScope.launch {
            val totalDiscount = factorViewModel.getTotalDiscountForFactor(args.factorId)
            val finalPrice = (sumPrice - totalDiscount) + sumVat

            // 3. آپدیت UI
            with(binding) {
                tvSumPrice.text = "${formatter.format(sumPrice)} ریال"
                tvSumDiscountPrice.text = "${"-" + formatter.format(totalDiscount)} ریال"
                tvSumVat.text = "${formatter.format(sumVat)} ریال"
                tvFinalPrice.text = "${formatter.format(finalPrice)} ریال"
            }

            // 🔑 تضمین استفاده از شناسه صحیح هدر
            val currentHeader = factorViewModel.factorHeader.value
            val correctHeaderId = if (args.factorId > 0) {
                args.factorId // در حالت ویرایش همیشه از شناسه آرگومان استفاده کن
            } else {
                currentHeader?.id ?: 0
            }


            // ✅ 4. آپدیت اتمیک هدر (بدون کوروتین تو در تو)
           // val currentHeader = factorViewModel.factorHeader.value ?: return@launch

            // ایجاد کپی با مقادیر جدید
          /*  val updatedHeader = currentHeader.copy(
                finalPrice = finalPrice,
                sabt = currentHeader.sabt // حفظ وضعیت فعلی sabt
            )*/
            // ایجاد هدر با شناسه صحیح
            val updatedHeader = currentHeader?.copy(
                id = correctHeaderId,
                finalPrice = finalPrice,
                sabt = currentHeader.sabt
            ) ?: FactorHeaderEntity(
                id = correctHeaderId,
                finalPrice = finalPrice,
                sabt = currentHeader.sabt            )

            // ابتدا حافظه را آپدیت کن
            factorViewModel.updateHeader(
                finalPrice = finalPrice,
                sabt = currentHeader.sabt
            )

            // سپس مستقیماً به دیتابیس بفرست (همان کوروتین)
            factorViewModel.updateFactorHeader(updatedHeader)

            Log.d(
                "DEBUG_OrderFragment",
                "finalPrice updated to DB: $finalPrice for factor ${currentHeader.id}"
            )
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
        backCallback?.remove()
        _binding = null
    }
}