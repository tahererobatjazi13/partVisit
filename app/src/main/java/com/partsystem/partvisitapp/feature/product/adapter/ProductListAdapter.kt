package com.partsystem.partvisitapp.feature.product.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import java.io.File
import java.text.DecimalFormat

@SuppressLint("NotifyDataSetChanged")
class ProductListAdapter(
    private val factorViewModel: FactorViewModel,
    private val factorId: Int,
    private val onProductChanged: (FactorDetailEntity) -> Unit,
    private val onClick: (ProductEntity) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    private val formatter = DecimalFormat("#,###")
    private var productEntities: List<ProductEntity> = emptyList()
    private var productWithAct: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useModel = false
    private var productValues: Map<Int, Pair<Double, Double>> =
        emptyMap() // productId → (unit1, packing)

    fun updateProductValues(values: Map<Int, Pair<Double, Double>>) {
        this.productValues = values
        notifyDataSetChanged()
    }

    fun setProductData(
        list: List<ProductEntity>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap(),
        values: Map<Int, Pair<Double, Double>> = emptyMap()
    ) {
        this.productEntities = list
        this.imagesMap = imagesMap
        this.productValues = values
        this.useModel = false
        notifyDataSetChanged()
    }

    fun setProductWithActData(
        list: List<ProductWithPacking>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productWithAct = list
        this.imagesMap = imagesMap
        this.useModel = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = if (useModel) productWithAct.size else productEntities.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (useModel) {
            val product = productWithAct[position]
            val images = imagesMap[product.product.id] ?: emptyList()
            holder.bind(product, images)
        } else {
            val product = productEntities[position]
            val images = imagesMap[product.id] ?: emptyList()
            holder.bind(product, images)
        }
    }

    @SuppressLint("SetTextI18n")
    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentProduct: ProductWithPacking? = null
        private var watcherUnit1: TextWatcher? = null
        private var watcherPacking: TextWatcher? = null
        private var packingTypedByUser = false

        fun bind(product: ProductEntity, images: List<ProductImageEntity>) = with(binding) {
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name ?: ""}"
            tvUnitName.text = product.unitName ?: ""
            llPrice.gone()
            cvProductPacking.gone()
            clAmount.gone()

            if (images.isNotEmpty()) {
                Glide.with(ivProduct.context)
                    .load(File(images.first().localPath.toString()))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }
            root.setOnClickListener { onClick(product) }
        }


        fun bind(product: ProductWithPacking, images: List<ProductImageEntity>) = with(binding) {
            currentProduct = product

            llPrice.show()
            clAmount.show()
            cvProductPacking.show()
            clUnitName.gone()

            root.isClickable = false
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvPrice.text = "قیمت: ${formatter.format(product.finalRate)} ریال"

            if (images.isNotEmpty()) {
                Glide.with(ivProduct.context)
                    .load(File(images.first().localPath.toString()))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }

            setupInputs(product)
            setupButtons()
            setupSpinner(product)
        }


        private fun setupInputs(product: ProductWithPacking) {
            watcherUnit1?.let { binding.etUnit1Value.removeTextChangedListener(it) }
            watcherPacking?.let { binding.etPackingValue.removeTextChangedListener(it) }

            val savedValues = this@ProductListAdapter.productValues[product.product.id]
            val unit1 = savedValues?.first ?: 0.0
            binding.etUnit1Value.setText(
                if (unit1 % 1 == 0.0) unit1.toInt().toString() else unit1.toString()
            )


            watcherUnit1 = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    notifyChange()
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
                    notifyChange()
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
            val adapter = SpinnerAdapter(itemView.context, names.toMutableList())
            binding.spProductPacking.adapter = adapter

            val default = product.packings.indexOfFirst { it.isDefault == true }
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
                        notifyChange()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }


        private fun notifyChange() {
            val product = currentProduct ?: return
            val packing =
                product.packings.getOrNull(binding.spProductPacking.selectedItemPosition)

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

            val detail = FactorDetailEntity(
                factorId = factorId,
                sortCode = bindingAdapterPosition,
                productId = product.product.id,
                anbarId = factorViewModel.factorHeader.value?.defaultAnbarId,
                actId = factorViewModel.factorHeader.value?.actId,
                unit1Value = finalUnit1,
                packingValue = finalPackingValue,
                unit2Value = 0.0,
                price = product.finalRate,
                packingId = packing?.packingId,
                vat = 0.0,
                unit1Rate = product.finalRate,
            )

            factorViewModel.productInputCache[product.product.id] =
                Pair(finalUnit1, finalPackingValue)

            detail.applyProduct(product)
            detail.applyPacking(packing)

            onProductChanged(detail)
        }

    }
}
