package com.partsystem.partvisitapp.feature.product.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
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
    private val onAddToCart: (ProductEntity, Int) -> Unit = { _, _ -> },
    private val currentQuantities: Map<Int, Int> = emptyMap(),
    private val onClick: (ProductEntity) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    private val formatter = DecimalFormat("#,###")
    private lateinit var productPacking: SpinnerAdapter

    private var productEntities: List<ProductEntity> = emptyList()
    private var productWithAct: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useModel = false

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
            holder.bind(product, images)   // ✔ درست
        } else {
            val product = productEntities[position]
            val images = imagesMap[product.id] ?: emptyList()
            holder.bind(product, images)
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

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

            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvUnitName.text = "" // در ProductModel unitName نداریم، می‌توان اضافه کرد اگر لازم باشد
            tvPrice.text = formatter.format(product.product.unitId ?: 0.0) + " ریال"

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

            watcher?.let { etAmount.removeTextChangedListener(it) }

               val quantity = currentQuantities[product.product.id] ?: 0
             if (etAmount.text.toString() != quantity.toString()) etAmount.setText(quantity.toString())

            watcher = object : TextWatcher {
                  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                  override fun afterTextChanged(s: Editable?) {
                      val newQty = s.toString().toIntOrNull() ?: 0
                      onAddToCart(product.product, newQty)
                  }
              }
            etAmount.addTextChangedListener(watcher)

            ivMax.setOnClickListener {
                val currentQty = etAmount.text.toString().toIntOrNull() ?: 0
                etAmount.setText((currentQty + 1).toString())
            }

            ivMin.setOnClickListener {
                val currentQty = etAmount.text.toString().toIntOrNull() ?: 0
                etAmount.setText((currentQty - 1).coerceAtLeast(0).toString())
            }

            root.setOnClickListener(null)
            val packingNames: MutableList<String> = product.packings
                .map { it.packingName ?: "" }
                .toMutableList()

            // اگر می‌خوای نمایش کاربر پسند باشه و موارد خالی رو حذف کنی:
            // val packingNames = product.packings.mapNotNull { it.packingName }.toMutableList()

            val spinnerAdapter = SpinnerAdapter(root.context, packingNames)
            spProductPacking.adapter = spinnerAdapter

            // تعیین انتخاب پیش‌فرض (اگر وجود داشته باشد)
            val defaultIndex = product.packings.indexOfFirst { it.isDefault == true }
            if (defaultIndex >= 0 && defaultIndex < packingNames.size) {
                spProductPacking.setSelection(defaultIndex)
            } else if (packingNames.isNotEmpty()) {
                spProductPacking.setSelection(0)
            }

            // هندل انتخاب کاربر
            spProductPacking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedPacking = product.packings.getOrNull(position)
                    // اینجا می‌تونی callback بزنی یا ViewModel رو آپدیت کنی:
                    // onPackingSelected(product.product.id, selectedPacking?.id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */
                }
            }

            clUnitName.gone()
            llPrice.show()
            clAmount.show()
            root.setOnClickListener(null)
        }
    }
}

