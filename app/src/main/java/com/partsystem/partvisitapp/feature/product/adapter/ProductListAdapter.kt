package com.partsystem.partvisitapp.feature.product.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import java.io.File
import java.text.DecimalFormat

class ProductListAdapter(
    private val fromFactor: Boolean = false,
    private val onProductChanged: (FactorDetailEntity) -> Unit,
    private val currentQuantities: Map<Int, Int> = emptyMap(),
    private val onClick: (ProductEntity) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {
    private val formatter = DecimalFormat("#,###")
    private var productEntities: List<ProductEntity> = emptyList()
    private var productWithAct: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useModel = false
    private lateinit var productPackingAdapter: SpinnerAdapter

    fun setProductData(
        list: List<ProductEntity>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productEntities = list
        this.imagesMap = imagesMap
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

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var watcherUnit1Value: TextWatcher? = null
        private var watcherPackingValue: TextWatcher? = null

        // برای ProductEntity
        fun bind(product: ProductEntity, images: List<ProductImageEntity>) = with(binding) {
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name ?: ""}"
            tvUnitName.text = product.unitName ?: ""

            // نمایش تصویر
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(ivProduct.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }
            llPrice.gone()
            cvProductPacking.gone()
            clAmount.gone()
            root.setOnClickListener { onClick(product) }
        }

        // برای ProductModel
        fun bind(product: ProductWithPacking, images: List<ProductImageEntity>) = with(binding) {

            llPrice.show()
            clAmount.show()
            cvProductPacking.show()
            clUnitName.gone()

            // حذف Ripple در این حالت
            root.apply {
                isClickable = false
                isFocusable = false
                rippleColor = null
                foreground = null
            }
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvUnitName.text = "" // در ProductModel unitName نداریم، می‌توان اضافه کرد اگر لازم باشد
            tvPrice.text = "قیمت: ${formatter.format(product.finalRate)}" + " ریال"

            // نمایش تصویر
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(ivProduct.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }

            watcherUnit1Value?.let { etUnit1Value.removeTextChangedListener(it) }
            watcherPackingValue?.let { etPackingValue.removeTextChangedListener(it) }

            val quantity = currentQuantities[product.product.id] ?: 0
            if (etUnit1Value.text.toString() != quantity.toString()) etUnit1Value.setText(quantity.toString())

            watcherUnit1Value = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                    val unit1Value = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val packingValue = etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
                    val selectedPacking = product.packings.getOrNull(spProductPacking.selectedItemPosition)

                    val detail = FactorDetailEntity(
                    //    id = product.existingFactorId,   // ‼️ باید مقدار واقعی ذخیره‌شده داشته باشد
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        actId = 195,
                        unit1Value = unit1Value,
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking?.id,
                        packingValue = packingValue,
                        vat = 0.0
                    )
                    onProductChanged(detail)
                }
            }

            watcherUnit1Value = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {

                    val unit1Value = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val packingValue = etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

                    val selectedPacking =
                        product.packings.getOrNull(spProductPacking.selectedItemPosition)

                    val detail = FactorDetailEntity(
                        id = null,
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        actId = 195,
                        unit1Value = unit1Value,
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking?.id,
                        packingValue = packingValue,
                        vat = 0.0
                    )
                    detail.applyProduct(product)

                    onProductChanged(detail)
                }
            }

            watcherPackingValue = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {

                    val unit1Value = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val packingValue = etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

                    val selectedPacking =
                        product.packings.getOrNull(spProductPacking.selectedItemPosition)

                    val detail = FactorDetailEntity(
                        id = null,
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        actId = 195,
                        unit1Value = unit1Value,
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking?.id,
                        packingValue = packingValue,
                        vat = 0.0
                    )
                    detail.applyProduct(product)

                    onProductChanged(detail)
                }
            }

            etUnit1Value.addTextChangedListener(watcherUnit1Value)
            etPackingValue.addTextChangedListener(watcherPackingValue)

            ivMax.setOnClickListener {
                val currentQty = etUnit1Value.text.toString().toIntOrNull() ?: 0
                etUnit1Value.setText((currentQty + 1).toString())
            }

            ivMin.setOnClickListener {
                val currentQty = etUnit1Value.text.toString().toIntOrNull() ?: 0
                etUnit1Value.setText((currentQty - 1).coerceAtLeast(0).toString())
            }

            val packingNames: MutableList<String> = product.packings
                .map { it.packingName ?: "" }
                .toMutableList()

            // اگر می‌خوای نمایش کاربر پسند باشه و موارد خالی رو حذف کنی:
            // val packingNames = product.packings.mapNotNull { it.packingName }.toMutableList()

            productPackingAdapter = SpinnerAdapter(root.context, packingNames)
            spProductPacking.adapter = productPackingAdapter

            // تعیین انتخاب پیش‌فرض (اگر وجود داشته باشد)
            val defaultIndex = product.packings.indexOfFirst { it.isDefault == true }
            if (defaultIndex >= 0 && defaultIndex < packingNames.size) {
                spProductPacking.setSelection(defaultIndex)
            } else if (packingNames.isNotEmpty()) {
                spProductPacking.setSelection(0)
            }


            spProductPacking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedPacking = product.packings.getOrNull(position)

                    // مقدارهای فعلی
                    val unit1Value = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val packingValue = etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

                    // ساخت FactorDetail
                    val detail = FactorDetailEntity(
                        id = null,
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        actId = 195,
                        unit1Value = unit1Value,
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking?.id,
                        packingValue = packingValue,
                        vat = 0.0
                    )

                    // 👈 این‌جا مقدار Packing را کامل اعمال و ذخیره می‌کنیم
                    detail.applyPacking(selectedPacking)

                    // 👈 ذخیره در Room
                    //    viewModel.saveFactorItem(factorItem)

                    // اگر لازم داری در سبد ثبت کنی
                    onProductChanged(detail)

                    // به‌روزرسانی Adapter
                    productPackingAdapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        }
    }
}

