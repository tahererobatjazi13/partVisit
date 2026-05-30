package com.partsystem.partvisitapp.feature.product.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
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
import java.io.File
import java.text.DecimalFormat

@SuppressLint("NotifyDataSetChanged")
class ProductListAdapter(
    private val onClickDetail: (ProductEntity) -> Unit = {},
    private val onClickDialog: (ProductWithPacking) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    private val formatter = DecimalFormat("#,###")
    private var normalProducts: List<ProductEntity> = emptyList()
    private var actProducts: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useActModel = false
    private var productValues: Map<Int, Pair<Double, Double>> =
        emptyMap() // productId → (unit1, packing)

    fun updateProductValues(values: Map<Int, Pair<Double, Double>>) {
        this.productValues = values
        notifyDataSetChanged()
    }

    fun setProductData(
        products: List<ProductEntity>,
        images: Map<Int, List<ProductImageEntity>> = emptyMap(),
        values: Map<Int, Pair<Double, Double>> = emptyMap()
    ) {
        this.normalProducts = products
        this.imagesMap = images
        this.productValues = values
        this.useActModel = false

        notifyDataSetChanged()
    }

    fun setProductWithActData(
        products: List<ProductWithPacking>,
        images: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        imagesMap = images
        useActModel = true

        // اول محصولاتی که در فاکتور هستند
        actProducts = products.sortedWith(
            compareByDescending<ProductWithPacking> {
                productValues.containsKey(it.product.id)
            }.thenBy { it.product.id })

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = if (useActModel) actProducts.size else normalProducts.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (useActModel) {
            val item = actProducts[position]
            holder.bindWithAct(
                product = item,
                images = imagesMap[item.product.id].orEmpty()
            )
        } else {
            val item = normalProducts[position]
            holder.bindNormal(
                product = item,
                images = imagesMap[item.id].orEmpty()
            )
        }
    }

    @SuppressLint("SetTextI18n")
    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindNormal(
            product: ProductEntity,
            images: List<ProductImageEntity>
        ) = with(binding) {

            llPrice.gone()

            tvNameProduct.text =
                "${bindingAdapterPosition + 1}_ ${product.name.orEmpty()}"

            tvUnitName.text =
                product.unitName.orEmpty()

            loadImage(images)

            root.setOnClickListener {
                onClickDetail(product)
            }
        }


        fun bindWithAct(
            product: ProductWithPacking,
            images: List<ProductImageEntity>
        ) = with(binding) {

            llPrice.show()
            root.isClickable = false

            tvNameProduct.text =
                "${bindingAdapterPosition + 1}_ ${product.product.name.orEmpty()}"

            tvPrice.text =
                "قیمت: ${formatter.format(product.finalRate)} ریال"

            tvUnitName.text =
                product.product.unitName.orEmpty()

            val backgroundColor =
                if (productValues.contains(product.product.id)) {
                    itemView.context.getColorFromAttr(R.attr.colorItem)
                } else {
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                }

            root.setCardBackgroundColor(backgroundColor)

            loadImage(images)

            root.setOnClickListener {
                onClickDialog(product)
            }
        }

        private fun loadImage(
            images: List<ProductImageEntity>
        ) {
            if (images.isEmpty()) {
                binding.ivProduct.setImageResource(R.drawable.ic_placeholder)
                return
            }
            Glide.with(binding.ivProduct.context)
                .load(File(images.first().localPath.orEmpty()))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivProduct)
        }
    }
}
