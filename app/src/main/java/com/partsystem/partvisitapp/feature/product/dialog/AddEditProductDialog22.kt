/*
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
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.DialogAddEditProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.product.repository.ProductRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class AddEditProductDialog(
    private val product: ProductWithPacking,
    private val onSave: (Double, Double, Int, Int, Int) -> Unit,
) : DialogFragment() {

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var mainPreferences: MainPreferences

    private lateinit var binding: DialogAddEditProductBinding
    private var currentProduct: ProductWithPacking? = null
    private var watcherUnit1: TextWatcher? = null
    private var watcherPacking: TextWatcher? = null
    private var packingTypedByUser = false
    private var detailId = 0
    private var productValues: Map<Int, Pair<Double, Double>> = emptyMap()
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private var currentMojoodiSetting: Int = 1 // Default
    private var warehouseId: Int = 0
    private var persianDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get warehouse ID and date from current factor header
        lifecycleScope.launch {

            warehouseId = mainPreferences.defaultAnbarId.first() ?: 0
            persianDate = getTodayPersianDate()

            // Load DistributionMojoodi setting
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

        binding.clConfirm.setOnClickListener {
            Log.d("DistributionMojoodi0", "ok")

            validateAndSaveProduct()
        }

        binding.clCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun validateAndSaveProduct() {
        val product = currentProduct
        val packing =
            product!!.packings.getOrNull(binding.spProductPacking.selectedItemPosition)

        val inputUnit1 =
            binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0

        val inputPacking =
            if (packingTypedByUser)
                binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
            else
                0.0
        Log.d("DistributionMojoodi1", "ok")

        var finalUnit1 = 0.0
        var finalPackingValue = 0.0

        if (packing != null) {
            val unitPerPack = packing.unit1Value
            when {
                // packing منبع است فقط اگر تایپ شده
                packingTypedByUser && inputPacking > 0 -> {
                    finalUnit1 = inputPacking * unitPerPack
                    finalPackingValue = inputPacking
                }
                // unit منبع است
                inputUnit1 > 0 -> {
                    finalUnit1 = inputUnit1
                    finalPackingValue =
                        if (unitPerPack > 0) finalUnit1 / unitPerPack else 0.0
                }
            }
            Log.d("DistributionMojoodi2", "ok")

        } else {
            Log.d("DistributionMojoodi3", "ok")

            finalUnit1 = inputUnit1
            finalPackingValue = 0.0
        }
        val unit1Value = binding.etUnit1Value.text.toString()
        val packingValue = binding.etPackingValue.text.toString()


        // Validate inputs
        if ((finalUnit1 == 0.0 && finalPackingValue == 0.0) ||
            (binding.etUnit1Value.text.toString() == "0" && binding.etPackingValue.text.isNullOrBlank())
        ) {
            Log.d("DistributionMojoodi4", "ok")

            Toast.makeText(context, R.string.error_request_amounts, Toast.LENGTH_SHORT).show()
            return
        }

        // Handle based on DistributionMojoodi setting
        when (currentMojoodiSetting) {
            1-> {
                Log.d("DistributionMojoodi5", "ok")
                // NoAction: Save directly without inventory check
                saveProduct(finalUnit1, finalPackingValue, packing!!)
            }

            2,3 -> {
                Log.d("DistributionMojoodi6", "ok")
                // Warning or Error: Check inventory before saving
                checkInventoryAndSave(finalUnit1, finalPackingValue, packing!!)
                Log.d("DistributionMojoodifinalUnit1", finalUnit1.toString())
                Log.d("DistributionMojoodifinalfinalPackingValue", finalPackingValue.toString())

            }

            else -> {
                // Default behavior: Save directly
                saveProduct(finalUnit1, finalPackingValue, packing!!)
            }
        }
    }

    private fun checkInventoryAndSave(
        requestedAmount: Double,
        packingValue: Double,
        packing: ProductPackingEntity
    ) {

        lifecycleScope.launch {
            val productId = currentProduct?.product?.id ?: 0
            Log.d("DistributionwarehouseId",warehouseId.toString())
            Log.d("DistributionproductId",productId.toString())
            Log.d("DistributionpersianDate",persianDate.toString())

            when (val result = productRepository.checkProductInventory(
                warehouseId,
                productId,
                persianDate
            )) {
                is NetworkResult.Loading -> {
                    // UI feedback during API call
                    binding.tvConfirm.isEnabled = false
                    binding.tvConfirm.gone()

                    // Show loading indicator if available in layout
                    binding.pbConfirm.show()
                }

                is NetworkResult.Success -> {
                    val availableStock = result.data
                    Log.d("DistributionMojoodiavailableStock", availableStock.toString())

                    if (requestedAmount > availableStock) {
                        Log.d("DistributionMojoodiarequestedAmount", requestedAmount.toString())
                        Log.d("DistributionMojoodavailableStock", availableStock.toString())

                        // Insufficient stock - show error based on setting
                        val errorMessage = when (currentMojoodiSetting) {
                            2 -> "⚠️ هشدار موجودی:\nموجودی کافی نیست!\nدرخواستی: ${
                                "%.2f".format(
                                    requestedAmount
                                )
                            }\nموجودی: ${"%.2f".format(availableStock)}"

                            3 -> "❌ خطا در موجودی:\nامکان ثبت فاکتور وجود ندارد!\nدرخواستی: ${
                                "%.2f".format(
                                    requestedAmount
                                )
                            }\nموجودی: ${"%.2f".format(availableStock)}"

                            else -> "موجودی کافی نیست (${availableStock} موجود)"
                        }

                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                context,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                            resetUiState()
                        }
                    } else {
                        // Sufficient stock - proceed to save
                        requireActivity().runOnUiThread {
                            saveProduct(requestedAmount, packingValue, packing)
                        }
                    }
                }

                is NetworkResult.Error -> {
                    // Handle API errors based on setting
                    val errorMessage = when (currentMojoodiSetting) {
                        2 -> "⚠️ هشدار:\nامکان بررسی موجودی وجود ندارد.\nمی‌توانید با احتیاط ادامه دهید."
                        3 -> "❌ خطا:\nامکان بررسی موجودی وجود ندارد.\nثبت فاکتور مسدود شد."
                        else -> "خطا در بررسی موجودی: ${result.message}"
                    }

                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            context,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()

                        // Only allow save in Warning mode when API fails
                        if (currentMojoodiSetting == 2) {
                            // Optional: Show confirmation dialog before saving without inventory check
                            saveProduct(requestedAmount, packingValue, packing)
                        } else {
                            resetUiState()
                        }
                    }
                }
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
        binding.etUnit1Value.setText(
            if (unit1 % 1 == 0.0) unit1.toInt().toString() else unit1.toString()
        )

        watcherUnit1 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        watcherPacking = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                packingTypedByUser = !s.isNullOrBlank()
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
            packingTypedByUser = false
            binding.etPackingValue.setText("")
            binding.etUnit1Value.setText((current + 1).toString())
        }

        binding.ivMin.setOnClickListener {
            val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
            packingTypedByUser = false
            binding.etPackingValue.setText("")
            binding.etUnit1Value.setText((current - 1).coerceAtLeast(0).toString())
        }
    }

    private fun setupSpinner(product: ProductWithPacking) {
        val names = product.packings.map { it.packingName ?: "" }
        val adapter = SpinnerAdapter(requireContext(), names.toMutableList())
        binding.spProductPacking.adapter = adapter

        val default = product.packings.indexOfFirst { it.isDefault }
        binding.spProductPacking.setSelection(if (default >= 0) default else 0)

        var init = false
        binding.spProductPacking.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    if (!init) {
                        init = true
                        return
                    }
                    // Handle spinner change if needed
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun observeCartData(productId: Int) {
        val validFactorId =
            factorViewModel.currentFactorId.value ?: factorViewModel.header.value!!.id.toLong()

        Log.d("DistributionvalidFactorId", validFactorId.toString())

        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                val values = mutableMapOf<Int, Pair<Double, Double>>()
                details.forEach { detail ->
                    if (productId == detail.productId) {
                        detailId = detail.id
                        Log.d("productIdDetailIdDialog", detailId.toString())

                        val cached = factorViewModel.productInputCache[detail.productId]
                        if (cached != null) {
                            values[detail.productId] = cached
                        } else {
                            val packingSize = detail.packing?.unit1Value ?: 0.0
                            if (packingSize > 0) {
                                val pack = floor(detail.unit1Value / packingSize)
                                val unit = detail.unit1Value % packingSize
                                values[detail.productId] = Pair(unit, pack)
                            } else {
                                values[detail.productId] = Pair(detail.unit1Value, 0.0)
                            }
                        }
                    }
                }
                updateProductValues(values, productId, detailId)
            }
    }

    private fun updateProductValues(
        values: Map<Int, Pair<Double, Double>>,
        productId: Int,
        detailId: Int
    ) {
        this.productValues = values

        values[productId]?.let { (unit1Value, packingValue) ->
            binding.etUnit1Value.setText(
                if (unit1Value % 1 == 0.0) unit1Value.toInt().toString() else unit1Value.toString()
            )

            if (packingTypedByUser) {
                binding.etPackingValue.setText(
                    if (packingValue % 1 == 0.0) packingValue.toInt()
                        .toString() else packingValue.toString()
                )
            } else {
                binding.etPackingValue.setText("")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


}*/
