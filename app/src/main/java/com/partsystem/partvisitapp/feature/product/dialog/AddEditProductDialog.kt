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
    private var watcherPacks: TextWatcher? = null
    private var watcherRemain: TextWatcher? = null

    private var currentProduct: ProductWithPacking? = null

    private var detailId = 0
    private var defaultAnbarId = 0
    private var persianDate: String = ""
    private var currentMojoodiSetting: Int = 1 // Default
    private var mojoodiConsumed = false

    private var finalUnit1 = 0.0
    private var finalPackingValue = 0.0
    private var isProcessing = false
    private var isUpdatingUnit = false
    private var isUpdatingPacking = false

    private enum class PackingFocus { NONE, PACKS, REMAIN }

    private var packingFocus: PackingFocus = PackingFocus.NONE

    private enum class PackingUpdateSource { NONE, FROM_UNIT, FROM_PACKS, FROM_REMAIN }

    private var packingUpdateSource: PackingUpdateSource = PackingUpdateSource.NONE

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

        val packingSize = selectedPacking?.unit1Value ?: 0.0
        val unitFromEt = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0

        finalUnit1 = if (packingSize > 0) {
            val packs = readPacks()
            val remain = readRemain()
            val total = (packs * packingSize) + remain
            if (total > 0) total else unitFromEt
        } else {
            unitFromEt
        }

        finalPackingValue = if (packingSize > 0) {
            finalUnit1 / packingSize
        } else {
            0.0
        }

        factorViewModel.productInputCache[product.product.id] = Pair(finalUnit1, finalPackingValue)

        if (finalUnit1 == 0.0) {
            Toast.makeText(context, R.string.error_request_amounts, Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        binding.clConfirm.isEnabled = false
        binding.tvConfirm.hide()
        binding.pbConfirm.show()

        when (currentMojoodiSetting) {
            1 -> {
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

    // ایجاد متد برای مدیریت لودینگ
    private suspend fun saveProductWithLoading(
        finalUnit1: Double,
        finalPackingValue: Double,
        packing: ProductPackingEntity?
    ) {
        factorViewModel.startProductSaving()

        val packingId = packing?.packingId ?: 0
        onSave(
            finalUnit1,
            finalPackingValue,
            packingId,
            detailId,
            product.product.id
        )

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
        watcherPacks?.let { binding.etPackingPacks.removeTextChangedListener(it) }
        watcherRemain?.let { binding.etPackingRemain.removeTextChangedListener(it) }

        val savedValues = this.productValues[product.product.id]
        val unit1 = savedValues?.first ?: 0.0

        // unit1 = تعداد کل واحد
        binding.etUnit1Value.setText(
            when {
                unit1 == 0.0 -> ""
                unit1 % 1 == 0.0 -> unit1.toInt().toString()
                else -> unit1.toString()
            }
        )

        // مقداردهی اولیه packs/remain از روی unit1
        val packingSize = selectedPacking?.unit1Value ?: 0.0
        if (packingSize > 0 && unit1 > 0) {
            val packsInit = floor(unit1 / packingSize).toInt()
            val remainInit = (unit1 - (packsInit * packingSize)).toInt()
            isUpdatingPacking = true
            setPackingFields(packsInit, remainInit)
            isUpdatingPacking = false
        } else {
            isUpdatingPacking = true
            setPackingFields(0, 0)
            isUpdatingPacking = false
        }

        // وقتی unit1 تغییر می‌کند => packs/remain آپدیت شود
        watcherUnit1 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUnit || isUpdatingPacking) return

                val packingSizeLocal = selectedPacking?.unit1Value ?: 0.0
                if (packingSizeLocal <= 0) return

                val totalUnits = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0

                if (totalUnits <= 0) {
                    packingUpdateSource = PackingUpdateSource.FROM_UNIT
                    isUpdatingPacking = true
                    setPackingFields(0, 0)
                    isUpdatingPacking = false
                    packingUpdateSource = PackingUpdateSource.NONE
                    return
                }

                val packs = floor(totalUnits / packingSizeLocal).toInt()
                val remain = (totalUnits - (packs * packingSizeLocal)).toInt()

                packingUpdateSource = PackingUpdateSource.FROM_UNIT
                isUpdatingPacking = true
                setPackingFields(packs, remain)
                isUpdatingPacking = false
                packingUpdateSource = PackingUpdateSource.NONE
            }
        }

        // تابع مشترک: packs/remain => unit1
        fun recalcUnitFromPackingFields() {
            if (isUpdatingPacking || isUpdatingUnit) return

            val packingSizeLocal = selectedPacking?.unit1Value ?: 0.0
            if (packingSizeLocal <= 0) return

            val packs = readPacks()
            val remain = readRemain()

            val totalUnits = (packs * packingSizeLocal) + remain

            isUpdatingUnit = true
            binding.etUnit1Value.setText(
                when {
                    totalUnits <= 0.0 -> ""
                    totalUnits % 1 == 0.0 -> totalUnits.toInt().toString()
                    else -> totalUnits.toString()
                }
            )
            binding.etUnit1Value.setSelection(binding.etUnit1Value.text?.length ?: 0)
            isUpdatingUnit = false
        }

        //وقتی packs تغییر می‌کند => unit1 آپدیت شود
        watcherPacks = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingPacking || isUpdatingUnit) return
                packingUpdateSource = PackingUpdateSource.FROM_PACKS
                recalcUnitFromPackingFields()
                packingUpdateSource = PackingUpdateSource.NONE
            }
        }

        //وقتی remain تغییر می‌کند => unit1 آپدیت شود
        watcherRemain = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingPacking || isUpdatingUnit) return
                packingUpdateSource = PackingUpdateSource.FROM_REMAIN
                recalcUnitFromPackingFields()
                packingUpdateSource = PackingUpdateSource.NONE
            }
        }
        binding.etPackingPacks.setOnFocusChangeListener { _, hasFocus ->
            packingFocus = if (hasFocus) PackingFocus.PACKS else PackingFocus.NONE
        }

        binding.etPackingRemain.setOnFocusChangeListener { _, hasFocus ->
            packingFocus = if (hasFocus) PackingFocus.REMAIN else PackingFocus.NONE
        }

        binding.etUnit1Value.addTextChangedListener(watcherUnit1)
        binding.etPackingPacks.addTextChangedListener(watcherPacks)
        binding.etPackingRemain.addTextChangedListener(watcherRemain)
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
        binding.spProductPacking.setSelection(0)

        val hasPackings = product.packings.isNotEmpty()
        val spinnerItems = if (hasPackings) {
            binding.etPackingPacks.isEnabled = true
            binding.etPackingRemain.isEnabled = true

            product.packings.map { it.packingName ?: "" }
        } else {
            binding.etPackingPacks.isEnabled = false
            binding.etPackingRemain.isEnabled = false
            isUpdatingPacking = true
            setPackingFields(0, 0)
            isUpdatingPacking = false

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
                        binding.etPackingPacks.isEnabled = true
                        binding.etPackingRemain.isEnabled = true
                    } else {
                        selectedPacking = null
                        binding.etPackingPacks.isEnabled = false
                        binding.etPackingRemain.isEnabled = false
                        isUpdatingPacking = true
                        setPackingFields(0, 0)
                        isUpdatingPacking = false
                    }
                    setupInputs(product)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.spProductPacking.setSelection(selectedIndex, false)
        selectedPacking = if (hasPackings) product.packings.getOrNull(selectedIndex) else null

        binding.etPackingPacks.isEnabled = hasPackings
        binding.etPackingRemain.isEnabled = hasPackings

        if (!hasPackings) {
            isUpdatingPacking = true
            setPackingFields(0, 0)
            isUpdatingPacking = false
        }
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

                        if (hasCurrentPackings) {

                            // پیدا کردن اندیسی که پکیج انتخاب‌شده در جزئیات دارد
                            var selectedIndex = currentProduct?.packings
                                ?.indexOfFirst { it.packingId == detail.packingId } ?: -1

                            // بسته‌بندی پیش‌فرض
                            if (selectedIndex < 0) {
                                selectedIndex = currentProduct?.packings
                                    ?.indexOfFirst { it.isDefault } ?: 0
                            }

                            // چک کردن اندیس
                            selectedIndex = selectedIndex.coerceIn(
                                0,
                                (currentProduct?.packings?.size ?: 1) - 1
                            )

                            // ست کردن اسپینر بدون تریگر شدن listener
                            binding.spProductPacking.setSelection(selectedIndex, false)

                            //  قبل از هر محاسبه، packing انتخاب‌شده را مشخص کن
                            selectedPacking = currentProduct?.packings?.get(selectedIndex)

                            // فعال شدن فیلد بسته‌بندی
                            binding.etPackingPacks.isEnabled = true
                            binding.etPackingRemain.isEnabled = true


                        } else {
                            binding.spProductPacking.setSelection(0, false)
                            selectedPacking = null
                            binding.etPackingPacks.isEnabled = false
                            binding.etPackingRemain.isEnabled = false
                            isUpdatingPacking = true
                            setPackingFields(0, 0)
                            isUpdatingPacking = false
                        }

                        // محاسبه صحیح واحد و بسته
                        val packingSize = selectedPacking?.unit1Value ?: 0.0
                        val values = mutableMapOf<Int, Pair<Double, Double>>()

                        if (packingSize > 0 && hasCurrentPackings) {
                            val totalUnits = detail.unit1Value
                            values[detail.productId] = Pair(totalUnits, 0.0)
                        } else {
                            values[detail.productId] = Pair(detail.unit1Value, 0.0)
                        }

                        // مقداردهی نهایی ورودی‌ها
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
            val packingSize = selectedPacking?.unit1Value ?: 0.0

            binding.etUnit1Value.setText(
                when {
                    unit1Value == 0.0 -> ""
                    unit1Value % 1 == 0.0 -> unit1Value.toInt().toString()
                    else -> unit1Value.toString()
                }
            )
            if (packingSize > 0 && unit1Value > 0) {
                val packs = floor(unit1Value / packingSize).toInt()
                val remain = (unit1Value - (packs * packingSize)).toInt()

                isUpdatingPacking = true
                setPackingFields(packs, remain)
                isUpdatingPacking = false
            } else {
                isUpdatingPacking = true
                setPackingFields(0, 0)
                isUpdatingPacking = false
            }
        }
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

    private fun readPacks(): Int {
        return binding.etPackingPacks.text?.toString()?.trim()?.toIntOrNull() ?: 0
    }

    private fun readRemain(): Int {
        return binding.etPackingRemain.text?.toString()?.trim()?.toIntOrNull() ?: 0
    }

    private fun setPackingFields(packs: Int, remain: Int) {

        // اگر آپدیت از UNIT آمد (مثلاً Max/Min):
        // - اگر remain>0 و packs==0 => packs را "0" نشان بده
        // - اگر packs>0 و remain==0 => remain را "0" نشان بده
        val showZeroForPacksByUnit =
            (packingUpdateSource == PackingUpdateSource.FROM_UNIT && remain > 0 && packs == 0)
        val showZeroForRemainByUnit =
            (packingUpdateSource == PackingUpdateSource.FROM_UNIT && packs > 0 && remain == 0)

        // اگر کاربر داخل remain تایپ کند، packs اگر صفر بود "0" باشد
        val showZeroForPacksByFocus = (packingFocus == PackingFocus.REMAIN)

        // اگر کاربر داخل packs تایپ کند، remain اگر صفر بود "0" باشد
        val showZeroForRemainByFocus = (packingFocus == PackingFocus.PACKS)

        val packsText = when {
            packs > 0 -> packs.toString()
            showZeroForPacksByFocus || showZeroForPacksByUnit -> "0"
            else -> ""
        }

        val remainText = when {
            remain > 0 -> remain.toString()
            showZeroForRemainByFocus || showZeroForRemainByUnit -> "0"
            else -> ""
        }

        if (binding.etPackingPacks.text?.toString() != packsText) {
            binding.etPackingPacks.setText(packsText)
        }
        if (binding.etPackingRemain.text?.toString() != remainText) {
            binding.etPackingRemain.setText(remainText)
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
