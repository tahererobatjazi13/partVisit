package com.partsystem.partvisitapp.feature.create_order.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.databinding.ItemOrderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import com.partsystem.partvisitapp.R


class OrderAdapter(
    private val loadProduct: suspend (Int, Int?) -> ProductWithPacking?,
    private val onQuantityChange: (FactorDetailEntity, Int) -> Unit,
    private val onDelete: (FactorDetailEntity) -> Unit,
) : ListAdapter<FactorDetailEntity, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val formatter = DecimalFormat("#,###,###,###")

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailEntity) = with(binding) {

            CoroutineScope(Dispatchers.IO).launch {
                // packing را resolve کن
                item.resolvePacking()
                var product = item.product
                if (product == null) {
                    product = loadProduct(item.productId, item.actId)
                    item.product = product
                    item.applyProduct(product!!)
                }

               if (item.isGift == 0) {
                          binding.root.setBackgroundColor(
                              ContextCompat.getColor(
                                  binding.root.context,
                                  R.color.yellow_EDDD50
                              )
                          )
                      } else {
                          binding.root.setBackgroundColor(
                              ContextCompat.getColor(
                                  binding.root.context,
                                  R.color.transparent
                              )
                          )
                      }

                withContext(Dispatchers.Main) {
                    tvName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
                    tvProductPacking.text = item.packingName

                    tvDiscountPrice.gone()
                    clDiscount.gone()
                    val displayUnit1 = item.unit1Value

                    // نمایش unit1Value بدون اعشار اضافه
                    val unit1Text = if (displayUnit1 % 1 == 0.0) {
                        item.unit1Value.toInt().toString()
                    } else {
                        item.unit1Value.toString()
                    }
                    etUnit1Value.setText(unit1Text)

                    // نمایش packing به فرمت "2 : 0"
                    etPackingValue.setText(item.getPackingValueFormatted())
                    etPackingValue.isEnabled = false


                    val total = item.price * item.unit1Value
                    tvPrice.text = formatter.format(total) + " ریال"

                    // Watcher برای تغییر دستی unit1 (اختیاری)
                    watcher?.let { etUnit1Value.removeTextChangedListener(it) }
                    watcher = object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val newQty = s.toString().toIntOrNull() ?: 0
                            onQuantityChange(item, newQty)
                        }

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
                    }
                    etUnit1Value.addTextChangedListener(watcher)

                    ivDelete.setOnClickListener { onDelete(item) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.vSeparator.visibility =
            if (position == itemCount - 1) View.GONE else View.VISIBLE
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<FactorDetailEntity>() {
    override fun areItemsTheSame(old: FactorDetailEntity, new: FactorDetailEntity) =
        old.productId == new.productId

    override fun areContentsTheSame(old: FactorDetailEntity, new: FactorDetailEntity) =
        old == new
}