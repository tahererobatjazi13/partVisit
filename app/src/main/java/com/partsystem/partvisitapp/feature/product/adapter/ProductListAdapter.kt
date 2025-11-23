package com.partsystem.partvisitapp.feature.product.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemProductBinding
import java.io.File
import java.text.DecimalFormat

class ProductListAdapter(
    private val typeShow: Boolean,
    private val onAddToCart: (ProductEntity, Int) -> Unit,
    private val currentQuantities: Map<Int, Int>,
    private val onClick: (ProductEntity) -> Unit = {},
    private var productImagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
) : ListAdapter<ProductEntity, ProductListAdapter.ProductItemViewHolder>(ProductDiffCallback()) {

    private val formatter = DecimalFormat("#,###")

    inner class ProductItemViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

        fun bind(product: ProductEntity) = with(binding) {
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name}"
            tvUnitName.text = product.unitName

            val images = productImagesMap[product.id]
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

            if (typeShow) {
                llPrice.show()
                clAmount.show()
                tvPrice.text = formatter.format(product.unitId) + " ریال"

                // حذف Watcher قبلی
                watcher?.let { etAmount.removeTextChangedListener(it) }

                // مقدار اولیه
                val quantity = currentQuantities[product.id] ?: 0
                if (etAmount.text.toString() != quantity.toString()) {
                    etAmount.setText(quantity.toString())
                }

                // TextWatcher جدید
                watcher = object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val newQty = s.toString().toIntOrNull() ?: 0
                        onAddToCart(product, newQty)
                    }
                }
                etAmount.addTextChangedListener(watcher)

                // دکمه افزایش
                ivMax.setOnClickListener {
                    val currentQty = etAmount.text.toString().toIntOrNull() ?: 0
                    etAmount.setText((currentQty + 1).toString())
                }

                // دکمه کاهش
                ivMin.setOnClickListener {
                    val currentQty = etAmount.text.toString().toIntOrNull() ?: 0
                    etAmount.setText((currentQty - 1).coerceAtLeast(0).toString())
                }

                root.setOnClickListener(null) // غیر فعال کردن کلیک روی آیتم

            } else {
                llPrice.gone()
                clAmount.gone()
                root.setOnClickListener { onClick(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItemViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ProductEntity>, imagesMap: Map<Int, List<ProductImageEntity>>) {
        productImagesMap = imagesMap
        submitList(data)
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
    override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity) =
        oldItem == newItem
}
