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
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.databinding.DialogAddEditProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import kotlin.math.floor

class AddEditProductDialog(
    private val product: ProductWithPacking,
    private val onSave: (Double, Double, Int, Int, Int) -> Unit,
) : DialogFragment() {

    private lateinit var binding: DialogAddEditProductBinding
    private var currentProduct: ProductWithPacking? = null
    private var watcherUnit1: TextWatcher? = null
    private var watcherPacking: TextWatcher? = null
    private var packingTypedByUser = false
    private var detailId = 0
    private var productValues: Map<Int, Pair<Double, Double>> =
        emptyMap() // productId → (unit1, packing)
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

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
        binding.btnConfirm.setOnClickListener {

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
            } else {
                finalUnit1 = inputUnit1
                finalPackingValue = 0.0
            }
            val unit1Value = binding.etUnit1Value.text.toString()
            val packingValue = binding.etPackingValue.text.toString()

            if (unit1Value == "0" && packingValue.isBlank()) {
                Toast.makeText(context, R.string.error_request_amounts, Toast.LENGTH_SHORT).show()
            } else {
                onSave(finalUnit1, finalPackingValue, packing!!.packingId, detailId,packing.productId)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
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
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        watcherPacking = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                packingTypedByUser = !s.isNullOrBlank()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

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
                        init = true; return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun observeCartData(productId: Int) {
        val validFactorId =
            factorViewModel.currentFactorId.value ?: factorViewModel.header.value!!.id.toLong()
        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                val values = mutableMapOf<Int, Pair<Double, Double>>()
                details.forEach { detail ->
                    if(productId==detail.productId){

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
                updateProductValues(values, productId,detailId)
            }}
    }

    // if(values[0]!!.equals(productId)){

    private fun updateProductValues(values: Map<Int, Pair<Double, Double>>, productId: Int, detailId: Int) {
        this.productValues = values
        val keys: List<Int> = values.keys.toList()

        for ((key, value) in values) {
            if (key == productId) {

                val unit1Value = value.first
                val packingValue = value.second
                binding.etUnit1Value.setText(
                    if (unit1Value % 1 == 0.0) unit1Value.toInt()
                        .toString() else unit1Value.toString()
                )

                if (packingTypedByUser) {
                    if (packingValue % 1 == 0.0) packingValue.toInt()
                        .toString() else packingValue.toString()
                } else {
                    binding.etPackingValue.setText("")

                }
            }
        }
    }

    //   }
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

