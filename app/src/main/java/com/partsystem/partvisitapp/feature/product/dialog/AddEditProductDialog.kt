package com.partsystem.partvisitapp.feature.product.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.DialogAddEditProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import com.partsystem.partvisitapp.feature.product.ui.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class AddEditProductDialog(
    private val productViewModel: ProductViewModel,
    private val product: ProductWithPacking,
    private val onSave: (Double, Double, Int, Int, Int) -> Unit,
) : DialogFragment() {

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var mainPreferences: MainPreferences

    private lateinit var binding: DialogAddEditProductBinding
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private var selectedPacking: ProductPackingEntity? = null
    private var productValues: Map<Int, Pair<Double, Double>> = emptyMap()

    private var watcherUnit1: TextWatcher? = null
    private var watcherPacking: TextWatcher? = null

    private var currentProduct: ProductWithPacking? = null

    private var detailId = 0
    private var defaultAnbarId = 0
    private var persianDate: String = ""
    private var currentMojoodiSetting: Int = 1 // Default
    private var mojoodiConsumed = false

    private var finalUnit1 = 0.0
    private var finalPackingValue = 0.0
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            defaultAnbarId = mainPreferences.defaultAnbarId.first() ?: 0
            persianDate = getTodayPersianDateLatin()
            currentMojoodiSetting = productRepository.getDistributionMojoodiSetting()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogAddEditProductBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)
        currentProduct = product
        observeMojoodi()

        updateDialogTitle()
        setupSpinner(product)
        setupInputs(product)
        setupButtons()

        product?.let {
            observeCartData(product.product.id)
        }
        binding.clConfirm.setOnClickListener {
            validateAndSaveProduct()
        }

        binding.clCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun validateAndSaveProduct() {
        if (isProcessing) return
        val product = currentProduct ?: return

        // ذخیره مقادیر اصلی کاربر در کش
        val originalUnit1 = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
        val originalPacking = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
        factorViewModel.productInputCache[product.product.id] = Pair(originalUnit1, originalPacking)

        // تغییر اصلی: تشخیص خودکار حالت بدون بسته‌بندی بر اساس وجود بسته‌بندی در محصول
        val hasPackings = product.packings.isNotEmpty()
        finalUnit1 = if (hasPackings && selectedPacking != null) {
            // با بسته‌بندی: محاسبه کل واحدها
            originalUnit1 + (originalPacking * selectedPacking!!.unit1Value)
        } else {
            // بدون بسته‌بندی: فقط واحد اول
            originalUnit1
        }

        finalPackingValue = if (hasPackings && selectedPacking != null) {
            finalUnit1 / selectedPacking!!.unit1Value
        } else {
            0.0 // بدون بسته‌بندی
        }

        // اعتبارسنجی
        if (finalUnit1 == 0.0) {
            Toast.makeText(context, R.string.error_request_amounts, Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        binding.clConfirm.isEnabled = false
        binding.tvConfirm.hide()
        binding.pbConfirm.show()
        // ذخیره نهایی
        when (currentMojoodiSetting) {
            1 -> {
                // ذخیره مستقیم بدون چک موجویی
                lifecycleScope.launch {
                    try {
                        saveProductWithLoading(finalUnit1, finalPackingValue, selectedPacking)
                    } finally {
                        isProcessing = false
                    }
                }
            }

            2, 3 -> {
                mojoodiConsumed = false
                productViewModel.checkMojoodi(
                    anbarId = defaultAnbarId,
                    productId = product.product.id,
                    persianDate = getTodayPersianDateLatin()
                )
            }

            else -> {
                lifecycleScope.launch {
                    try {
                        saveProductWithLoading(finalUnit1, finalPackingValue, selectedPacking)
                    } finally {
                        isProcessing = false
                    }
                }
            }
        }
    }

    // 3. ایجاد متد جدید برای مدیریت لودینگ
    private suspend fun saveProductWithLoading(
        finalUnit1: Double,
        finalPackingValue: Double,
        packing: ProductPackingEntity?
    ) {
        // کلید اصلی: ابتدا به ViewModel اطلاع دهیم که محاسبات شروع شده
        factorViewModel.startProductSaving()

        // فراخوانی کال‌بک با تأخیر کوتاه برای اطمینان از نمایش لودینگ
        //  delay(50)

        val packingId = packing?.packingId ?: 0
        onSave(
            finalUnit1,
            finalPackingValue,
            packingId,
            detailId,
            product.product.id
        )

        // انتظار برای اتمام کامل محاسبات در ViewModel
        factorViewModel.waitForProductSavingComplete()


    }

    private fun resetUiState() {
        isProcessing = false
        binding.clConfirm.isEnabled = true
        binding.tvConfirm.show()
        binding.pbConfirm.gone()
    }

    private fun setupInputs(product: ProductWithPacking) {
        watcherUnit1?.let { binding.etUnit1Value.removeTextChangedListener(it) }
        watcherPacking?.let { binding.etPackingValue.removeTextChangedListener(it) }

        val savedValues = this.productValues[product.product.id]
        val unit1 = savedValues?.first ?: 0.0

        binding.etUnit1Value.setText(
            if (unit1 % 1 == 0.0 && unit1 != 0.0) unit1.toInt().toString()
            else if (unit1 == 0.0) ""
            else unit1.toString()
        )

        val packing = savedValues?.second ?: 0.0
        binding.etPackingValue.setText(
            when {
                packing < 0.001 -> ""
                packing % 1 == 0.0 -> packing.toInt().toString()
                else -> packing.toString()
            }
        )

        watcherUnit1 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateEquivalentVisibility()
                updateEquivalentText()
            }
        }

        watcherPacking = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateEquivalentVisibility()
                updateEquivalentText()
            }
        }

        binding.etUnit1Value.addTextChangedListener(watcherUnit1)
        binding.etPackingValue.addTextChangedListener(watcherPacking)
    }

    @SuppressLint("SetTextI18n")
    private fun setupButtons() {
        binding.ivMax.setOnClickListener {
            val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
            binding.etUnit1Value.setText((current + 1).toString())

            updateEquivalentText()
            updateEquivalentVisibility()
        }

        binding.ivMin.setOnClickListener {
            val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
            binding.etUnit1Value.setText((current - 1).coerceAtLeast(0).toString())
        }
    }

    private fun updateEquivalentVisibility() {
        val unit1 = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
        val packing = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

        binding.tvEquivalent.visibility =
            if (unit1 > 0 || packing > 0) View.VISIBLE else View.GONE
    }

    private fun setupSpinner(product: ProductWithPacking) {
        binding.spProductPacking.setSelection(0)

        val hasPackings = product.packings.isNotEmpty()
        val spinnerItems = if (hasPackings) {
            product.packings.map { it.packingName ?: "" }
        } else {
            listOf("بدون بسته‌بندی")
        }

        val adapter = SpinnerAdapter(requireContext(), spinnerItems.toMutableList())
        binding.spProductPacking.adapter = adapter

        val selectedIndex = if (hasPackings) {
            val defaultIndex = product.packings.indexOfFirst { it.isDefault }
            if (defaultIndex in spinnerItems.indices) defaultIndex else 0
        } else {
            0
        }

        // تنظیم لیسنر قبل از setSelection برای جلوگیری از فراخوانی ناخواسته
        binding.spProductPacking.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    factorViewModel.productInputCache.remove(product.product.id)

                    if (hasPackings) {
                        selectedPacking = product.packings.getOrNull(pos)
                        // فقط وقتی بسته‌بندی وجود دارد، فیلد فعال شود
                        binding.etPackingValue.isEnabled = true
                        binding.etPackingValue.isFocusable = true
                        binding.etPackingValue.isFocusableInTouchMode = true
                    } else {
                        selectedPacking = null
                        binding.etPackingValue.isEnabled = false
                        binding.etPackingValue.isFocusable = false
                        binding.etPackingValue.setText("")
                    }

                    setupInputs(product)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // setSelection بعد از تنظیم لیسنر
        binding.spProductPacking.setSelection(selectedIndex, false)

        // تنظیم اولیه enabled state بر اساس وجود بسته‌بندی
        binding.etPackingValue.isEnabled = hasPackings
        binding.etPackingValue.isFocusable = hasPackings
        binding.etPackingValue.isFocusableInTouchMode = hasPackings
    }

    private fun observeCartData(productId: Int) {
        val validFactorId = factorViewModel.currentFactorId.value
            ?: factorViewModel.header.value?.id?.toLong() ?: return
        Log.d("ProductValuesIf", validFactorId.toString())

        Log.d("validFactorId", validFactorId.toString())
        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                details.forEach { detail ->
                    if (productId == detail.productId) {
                        Log.d("ProductValuesproductId0", productId.toString())
                        Log.d("ProductValuesproductId1", detail.productId.toString())

                        detailId = detail.id
                        updateDialogTitle()

                        val hasCurrentPackings = currentProduct?.packings?.isNotEmpty() == true
                        val hasPackingInDetail = detail.packingId != null && detail.packingId != 0

                        //--------------------------
                        // 1. ابتدا packing را درست ست کن
                        //--------------------------
                        if (hasCurrentPackings) {

                            // پیدا کردن اندیسی که پکیج انتخاب‌شده در جزئیات دارد
                            var selectedIndex = currentProduct?.packings
                                ?.indexOfFirst { it.packingId == detail.packingId } ?: -1

                            // اگر پیدا نشد: برو سراغ بسته‌بندی پیش‌فرض
                            if (selectedIndex < 0) {
                                selectedIndex = currentProduct?.packings
                                    ?.indexOfFirst { it.isDefault } ?: 0
                            }

                            // چک کردن ایمنی اندیس
                            selectedIndex = selectedIndex.coerceIn(
                                0,
                                (currentProduct?.packings?.size ?: 1) - 1
                            )

                            // ست کردن اسپینر بدون تریگر شدن listener
                            binding.spProductPacking.setSelection(selectedIndex, false)

                            // مهم: قبل از هر محاسبه، packing انتخاب‌شده را مشخص کن
                            selectedPacking = currentProduct?.packings?.get(selectedIndex)

                            // فعال شدن فیلد بسته‌بندی
                            binding.etPackingValue.isEnabled = true
                            binding.etPackingValue.isFocusable = true
                            binding.etPackingValue.isFocusableInTouchMode = true

                        } else {
                            binding.spProductPacking.setSelection(0, false)
                            selectedPacking = null

                            binding.etPackingValue.isEnabled = false
                            binding.etPackingValue.isFocusable = false
                            binding.etPackingValue.setText("")
                        }

                        //--------------------------
                        // 2. اگر cached هست، مستقیم از آن استفاده کن
                        //--------------------------
                        Log.d("ProductValuesproductId", detail.productId.toString())

                        /*    factorViewModel.productInputCache[detail.productId]?.let { cached ->
                                updateProductValues(
                                    mapOf(productId to cached),
                                    productId,
                                    detailId
                                )
                                return@forEach
                            }
    */
                        //--------------------------
                        // 3. محاسبه صحیح واحد و بسته
                        //--------------------------
                        val packingSize = selectedPacking?.unit1Value ?: 0.0
                        val values = mutableMapOf<Int, Pair<Double, Double>>()

                        if (packingSize > 0 && hasPackingInDetail && hasCurrentPackings) {

                            val totalUnits = detail.unit1Value
                            val packCount = floor(totalUnits / packingSize)
                            val looseUnits = totalUnits - (packCount * packingSize)

                            values[detail.productId] = Pair(looseUnits, packCount)

                        } else {
                            values[detail.productId] = Pair(detail.unit1Value, 0.0)
                            binding.etPackingValue.setText("")
                        }

                        //--------------------------
                        // 4. مقداردهی نهایی ورودی‌ها
                        //--------------------------
                        updateProductValues(values, productId, detailId)
                    }
                }
            }
    }

    private fun updateDialogTitle() {
        binding.tvTitleDialog.text =
            if (detailId > 0)
                getString(R.string.label_edit_product)
            else
                getString(R.string.label_add_product)
    }

    private fun updateProductValues(
        values: Map<Int, Pair<Double, Double>>,
        productId: Int,
        detailId: Int
    ) {

        Log.d("ProductValues", values.toString())

        this.productValues = values
        values[productId]?.let { (unit1Value, packingValue) ->

            binding.etUnit1Value.setText(
                if (unit1Value == 0.0) ""
                else if (unit1Value % 1 == 0.0) unit1Value.toInt().toString()
                else unit1Value.toString()
            )
            binding.etPackingValue.setText(
                when {
                    packingValue < 0.001 -> ""
                    packingValue % 1 == 0.0 -> packingValue.toInt().toString()
                    else -> packingValue.toString()
                }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateEquivalentText() {

        val unit1 = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
        val packingCount = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
        val packingSize = selectedPacking?.unit1Value ?: 0.0

        if (unit1 <= 0 && packingCount <= 0) {
            binding.tvEquivalent.gone()
            return
        }

        binding.tvEquivalent.show()

        // بدون بسته‌بندی
        if (packingSize <= 0) {
            binding.tvEquivalent.text = "معادل ${cleanNumber(unit1)}:0"
            return
        }

        // با بسته‌بندی
        val totalUnits = unit1 + (packingCount * packingSize)

        val fullPacks = floor(totalUnits / packingSize).toInt()
        val remainUnits = totalUnits - (fullPacks * packingSize)

        val remainText = if (remainUnits % 1 == 0.0)
            remainUnits.toInt().toString()
        else
            remainUnits.toString()

        binding.tvEquivalent.text = "معادل ${remainText} : ${fullPacks}"
    }

    private fun cleanNumber(num: Double): String {
        return if (num % 1 == 0.0) num.toInt().toString() else num.toString()
    }

    private fun observeMojoodi() {
        productViewModel.checkMojoodi.observe(viewLifecycleOwner) { result ->
            if (result == null || mojoodiConsumed) return@observe

            when (result) {
                is NetworkResult.Loading -> {
                    //  binding.tvConfirm.hide()
                    //  binding.pbConfirm.show()
                }

                is NetworkResult.Success -> {
                    mojoodiConsumed = true
                    productViewModel.clearCheckMojoodi()

                    if (result.data.isEmpty()) {
                        Toast.makeText(
                            context,
                            R.string.error_out_of_stock,
                            Toast.LENGTH_LONG
                        ).show()
                        resetUiState()
                        return@observe
                    }

                    val mojoodi = result.data.first()

                    if (finalUnit1 > mojoodi.mojoodi) {
                        Toast.makeText(
                            context,
                            R.string.error_insufficient_inventory,
                            Toast.LENGTH_LONG
                        ).show()
                        resetUiState()
                    } else {
                        lifecycleScope.launch {
                            saveProductWithLoading(
                                finalUnit1,
                                finalPackingValue,
                                selectedPacking
                            )
                        }
                    }
                }

                is NetworkResult.Error -> {
                    mojoodiConsumed = true
                    productViewModel.clearCheckMojoodi()

                    //   binding.tvConfirm.show()
                    //   binding.pbConfirm.gone()

                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                    resetUiState()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}