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
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
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
    private var selectedPacking: ProductPackingEntity? = null
    private lateinit var binding: DialogAddEditProductBinding
    private var currentProduct: ProductWithPacking? = null
    private var watcherUnit1: TextWatcher? = null
    private var watcherPacking: TextWatcher? = null
    private var detailId = 0
    private var productValues: Map<Int, Pair<Double, Double>> = emptyMap()
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private var currentMojoodiSetting: Int = 1 // Default
    private var defaultAnbarId = 0
    private var persianDate: String = ""
    private var mojoodiConsumed = false
    var finalUnit1 = 0.0
    var finalPackingValue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            defaultAnbarId = mainPreferences.defaultAnbarId.first() ?: 0
            persianDate = getTodayPersianDate()
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

        binding.tvTitleDialog.text = if (product == null) {
            getString(R.string.label_add_product)
        } else {
            getString(R.string.label_edit_product)
        }
        product?.let {
            observeCartData(product.product.id)
        }
        setupInputs(product)
        setupButtons()
        setupSpinner(product)

        selectedPacking =
            product.packings.getOrNull(binding.spProductPacking.selectedItemPosition)
        binding.clConfirm.setOnClickListener {
            validateAndSaveProduct()
        }

        binding.clCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun validateAndSaveProduct() {
        val product = currentProduct ?: return
        //val packing = product.packings.getOrNull(binding.spProductPacking.selectedItemPosition)

        val packing = selectedPacking ?: return

        // ذخیره مقادیر اصلی کاربر در کش — این خط کلید اصلی است
        val originalUnit1 = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
        val originalPacking = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
        factorViewModel.productInputCache[product.product.id] = Pair(originalUnit1, originalPacking)

        //  محاسبه مقادیر نهایی برای ذخیره در دیتابیس (همان منطق فعلی)
        val inputUnit1 = originalUnit1
        val inputPacking = originalPacking
        finalUnit1 = inputUnit1
        finalPackingValue = inputPacking

        if (packing != null && packing.unit1Value > 0) {
            val unitPerPack = packing.unit1Value

            //  محاسبه کل تعداد واحدها (unit1Value)
            finalUnit1 = originalUnit1 + (originalPacking * unitPerPack)

            //  محاسبه packingValue به صورت کسری (کل تعداد ÷ نسبت بسته‌بندی)
            finalPackingValue = finalUnit1 / unitPerPack

        } else {
            // اگر بسته‌بندی تعریف نشده، فقط واحدهای ساده ذخیره شود
            finalUnit1 = originalUnit1
            finalPackingValue = 0.0
        }
        // اعتبارسنجی
        if ((finalUnit1 == 0.0 && finalPackingValue == 0.0) ||
            (binding.etUnit1Value.text.toString() == "0" && binding.etPackingValue.text.isNullOrBlank())
        ) {
            Toast.makeText(context, R.string.error_request_amounts, Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("SelectedPacking2", selectedPacking?.packingName ?: "NULL")

        // ذخیره نهایی در دیتابیس (مقادیر محاسبه‌شده)
        /*  when (currentMojoodiSetting) {
              2,3 -> saveProduct(finalUnit1, finalPackingValue, selectedPacking!!)
              1 -> productViewModel.checkMojoodi(
                  anbarId = defaultAnbarId,
                  productId = product.product.id,
                  persianDate = getTodayPersianDate()
              )

              else -> saveProduct(finalUnit1, finalPackingValue, selectedPacking!!)
          }*/

        when (currentMojoodiSetting) {
            2, 3 -> {
                saveProduct(finalUnit1, finalPackingValue, selectedPacking!!)
            }

            1 -> {
                mojoodiConsumed = false
                productViewModel.checkMojoodi(
                    anbarId = defaultAnbarId,
                    productId = product.product.id,
                    persianDate = getTodayPersianDate()
                )
            }

            else -> {
                saveProduct(finalUnit1, finalPackingValue, selectedPacking!!)
            }
        }
    }

    private fun saveProduct(
        finalUnit1: Double,
        finalPackingValue: Double,
        packing: ProductPackingEntity
    ) {
        onSave(
            finalUnit1,
            finalPackingValue,
            packing!!.packingId,
            detailId,
            packing.productId
        )
        dismiss()
    }

    private fun resetUiState() {
        binding.tvConfirm.isEnabled = true
        binding.tvConfirm.show()
        binding.pbConfirm.gone()
    }

    private fun setupInputs(product: ProductWithPacking) {
        watcherUnit1?.let { binding.etUnit1Value.removeTextChangedListener(it) }
        watcherPacking?.let { binding.etPackingValue.removeTextChangedListener(it) }

        val savedValues = this.productValues[product.product.id]
        val unit1 = savedValues?.first ?: 0.0

        // نمایش واحد (بدون تغییر)
        binding.etUnit1Value.setText(
            if (unit1 % 1 == 0.0) unit1.toInt().toString() else unit1.toString()
        )

        // اگر مقدار صفر بود، خالی نمایش بده
        val packing = savedValues?.second ?: 0.0
        binding.etPackingValue.setText(
            when {
                packing < 0.001 -> ""
                packing % 1 == 0.0 -> packing.toInt().toString()
                else -> packing.toString()
            }
        )

        // TextWatchers بدون تغییر
        watcherUnit1 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        watcherPacking = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etUnit1Value.addTextChangedListener(watcherUnit1)
        binding.etPackingValue.addTextChangedListener(watcherPacking)
    }

    @SuppressLint("SetTextI18n")
    private fun setupButtons() {
        binding.ivMax.setOnClickListener {
            val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
            binding.etUnit1Value.setText((current + 1).toString())
        }

        binding.ivMin.setOnClickListener {
            val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
            binding.etUnit1Value.setText((current - 1).coerceAtLeast(0).toString())
        }
    }

    private fun setupSpinner(product: ProductWithPacking) {
        val names = product.packings.map { it.packingName ?: "" }
        val adapter = SpinnerAdapter(requireContext(), names.toMutableList())
        binding.spProductPacking.adapter = adapter

        val default = product.packings.indexOfFirst { it.isDefault }
        binding.spProductPacking.setSelection(if (default >= 0) default else 0)

        binding.spProductPacking.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    selectedPacking = product.packings[pos]

                    // پاک کردن کش
                    factorViewModel.productInputCache.remove(product.product.id)

                    // باز محاسبه UI
                    setupInputs(product)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

    }

    private fun observeCartData(productId: Int) {
        val validFactorId = factorViewModel.currentFactorId.value
            ?: factorViewModel.header.value?.id?.toLong() ?: return

        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                details.forEach { detail ->
                    if (productId == detail.productId) {
                        detailId = detail.id


                        // پیدا کردن packing ذخیره‌شده
                        val savedPackingIndex = currentProduct?.packings
                            ?.indexOfFirst { it.packingId == detail.packingId }
                            ?: -1

                        if (savedPackingIndex >= 0) {
                            binding.spProductPacking.setSelection(savedPackingIndex, false)
                            selectedPacking = currentProduct?.packings?.get(savedPackingIndex)
                        }

                        //  خواندن از کش (مقادیر اصلی کاربر)
                        val cached = factorViewModel.productInputCache[detail.productId]
                        if (cached != null) {
                            // مستقیماً از کش نمایش داده شود — این همان چیزی است که می‌خواهیم
                            updateProductValues(mapOf(productId to cached), productId, detailId)
                            return@forEach // خروج زودهنگام برای جلوگیری از اجراي بقیه کد
                        }

                        // ✅ اولویت 2: اگر کش وجود نداشت، تجزیه با استفاده از packingId صحیح
                        val packingForDetail =
                            currentProduct?.packings?.find { it.packingId == detail.packingId }
                        val packingSize = packingForDetail?.unit1Value ?: 0.0

                        val values = mutableMapOf<Int, Pair<Double, Double>>()
                        if (packingSize > 0) {
                            // 🔑 تجزیه صحیح: واحد باقیمانده + تعداد بسته‌های کامل
                            val totalUnits = detail.unit1Value
                            val packCount = floor(totalUnits / packingSize)
                            val looseUnits = totalUnits - (packCount * packingSize)

                            values[detail.productId] = Pair(looseUnits, packCount)
                        } else {
                            values[detail.productId] = Pair(detail.unit1Value, 0.0)
                        }
                        updateProductValues(values, productId, detailId)
                    }
                }
            }
    }
    /*
        private fun observeCartData(productId: Int) {
            val validFactorId = factorViewModel.currentFactorId.value
                ?: factorViewModel.header.value?.id?.toLong() ?: return

            if (validFactorId <= 0) return

            factorViewModel.getFactorDetails(validFactorId.toInt())
                .observe(viewLifecycleOwner) { details ->
                    details.forEach { detail ->
                        if (productId == detail.productId) {
                            detailId = detail.id

                            //  خواندن از کش (مقادیر اصلی کاربر)
                            val cached = factorViewModel.productInputCache[detail.productId]
                            if (cached != null) {
                                // مستقیماً از کش نمایش داده شود
                                updateProductValues(mapOf(productId to cached), productId, detailId)
                            } else {
                                //  اگر کش وجود نداشت، از دیتابیس بخوان و تجزیه کن (برای داده‌های قدیمی)
                                val packingSize = detail.packing?.unit1Value ?: 0.0
                                val values = mutableMapOf<Int, Pair<Double, Double>>()
                                if (packingSize > 0) {
                                    val pack = floor(detail.unit1Value / packingSize)
                                    val unit = detail.unit1Value % packingSize
                                    values[detail.productId] = Pair(unit, pack)
                                } else {
                                    values[detail.productId] = Pair(detail.unit1Value, 0.0)
                                }
                                updateProductValues(values, productId, detailId)
                            }
                        }
                    }
                }
        }
    */

    private fun updateProductValues(
        values: Map<Int, Pair<Double, Double>>,
        productId: Int,
        detailId: Int
    ) {
        this.productValues = values
        values[productId]?.let { (unit1Value, packingValue) ->
            // نمایش واحد (بدون تغییر)
            binding.etUnit1Value.setText(
                if (unit1Value % 1 == 0.0) unit1Value.toInt().toString() else unit1Value.toString()
            )

            //  اگر مقدار صفر یا نزدیک به صفر بود، خالی نمایش بده
            binding.etPackingValue.setText(
                when {
                    packingValue < 0.001 -> "" // صفر یا نزدیک به صفر → خالی
                    packingValue % 1 == 0.0 -> packingValue.toInt().toString()
                    else -> packingValue.toString()
                }
            )
        }
    }

    private fun observeMojoodi() {
        productViewModel.checkMojoodi.observe(viewLifecycleOwner) { result ->
            if (result == null || mojoodiConsumed) return@observe

            when (result) {

                is NetworkResult.Loading -> {
                    binding.tvConfirm.hide()
                    binding.pbConfirm.show()
                }

                is NetworkResult.Success -> {
                    mojoodiConsumed = true
                    productViewModel.clearCheckMojoodi()

                    binding.tvConfirm.show()
                    binding.pbConfirm.gone()

                    if (result.data.isNullOrEmpty()) {
                        Toast.makeText(
                            context,
                            R.string.error_out_of_stock,
                            Toast.LENGTH_LONG
                        ).show()
                        resetUiState()
                        return@observe
                    }

                    val mojoodi = result.data.first()

                    Log.d("mojoodifinalUnit1", finalUnit1.toString())
                    Log.d("mojoodimojoodi", mojoodi.mojoodi.toString())

                    if (finalUnit1 > mojoodi.mojoodi) {
                        Toast.makeText(
                            context,
                            R.string.error_insufficient_inventory,
                            Toast.LENGTH_LONG
                        ).show()
                        resetUiState()
                    } else {
                        saveProduct(finalUnit1, finalPackingValue, selectedPacking!!)
                        dismiss()
                    }
                }

                is NetworkResult.Error -> {
                    mojoodiConsumed = true
                    productViewModel.clearCheckMojoodi()

                    binding.tvConfirm.show()
                    binding.pbConfirm.gone()

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
