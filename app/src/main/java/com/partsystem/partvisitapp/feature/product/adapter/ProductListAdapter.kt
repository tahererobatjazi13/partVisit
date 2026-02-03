package com.partsystem.partvisitapp.feature.product.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemProductBinding
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import java.io.File
import java.text.DecimalFormat

@SuppressLint("NotifyDataSetChanged")
class ProductListAdapter(
    private val loadProduct: (Int, Int?) -> ProductWithPacking?,
    private val factorViewModel: FactorViewModel,
    private val factorId: Int,
    private val onProductChanged: (FactorDetailEntity) -> Unit,
    private val onClickDetail: (ProductEntity) -> Unit = {},
    private val onClickDialog: (ProductWithPacking) -> Unit = {}
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
        this.imagesMap = imagesMap
        this.useModel = true

        // اول محصولاتی که در فاکتور هستند
        this.productWithAct = list.sortedWith(
            compareByDescending<ProductWithPacking> {
                productValues.containsKey(it.product.id)
            }.thenBy {
                it.product.id
            }
        )

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

        fun bind(product: ProductEntity, images: List<ProductImageEntity>) = with(binding) {
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name ?: ""}"
            tvUnitName.text = product.unitName ?: ""
            llPrice.gone()

            if (images.isNotEmpty()) {
                Glide.with(ivProduct.context)
                    .load(File(images.first().localPath.toString()))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }
            root.setOnClickListener { onClickDetail(product) }
        }


        fun bind(product: ProductWithPacking, images: List<ProductImageEntity>) = with(binding) {

            llPrice.show()

            root.isClickable = false
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvPrice.text = "قیمت: ${formatter.format(product.finalRate)} ریال"
            tvUnitName.text = product.product.unitName ?: ""

            // تنظیم رنگ بر اساس موجودیت
            if (productValues.contains(product.product.id)) {
                root.setCardBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorItem))
            } else {
                root.setCardBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorBasic))
            }

            if (images.isNotEmpty()) {
                Glide.with(ivProduct.context)
                    .load(File(images.first().localPath.toString()))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }

            root.setOnClickListener { onClickDialog(product) }
        }
    }
}
