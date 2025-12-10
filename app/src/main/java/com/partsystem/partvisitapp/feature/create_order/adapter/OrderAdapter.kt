package com.partsystem.partvisitapp.feature.create_order.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.Discount
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.DialogSelectDiscountBinding
import com.partsystem.partvisitapp.databinding.ItemOrderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class OrderAdapter(
    private val loadProduct: suspend (Int, Int?) -> ProductWithPacking?,
    private val onQuantityChange: (FactorDetailEntity, Int) -> Unit,
    private val onDelete: (FactorDetailEntity) -> Unit,
) : ListAdapter<FactorDetailEntity, OrderAdapter.OrderViewHolder>(
    OrderDiffCallback()
) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var watcher: TextWatcher? = null

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailEntity) = with(binding) {


            CoroutineScope(Dispatchers.Main).launch {
                val product =
                    loadProduct(item.productId!!, item.actId)

                product?.let {
                    tvName.text = "${bindingAdapterPosition + 1}_ ${product.product.name}"
                }
            }

            etUnit1Value.setText(item.unit1Value.toString())
            //tvProductPacking.text =item.packingValue
            if (bindingAdapterPosition % 2 == 0) {
                clDiscount.show()
                tvDiscountPrice.show()
                tvPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvDiscountPrice.text = formatter.format(item.price) + " ریال"

            } else {
                clDiscount.gone()
                tvDiscountPrice.gone()
                tvPrice.text = formatter.format(item.price) + " ریال"

            }
            // حذف Watcher قبلی
            watcher?.let {
                etUnit1Value.removeTextChangedListener(it)
            }

            val quantity = item.unit1Value
            if (etUnit1Value.text.toString() != quantity.toString()) {
                etUnit1Value.setText(quantity.toString())
            }

            // ساخت Watcher جدید
            watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newQty = s.toString().toIntOrNull() ?: 0
                    onQuantityChange(item, newQty)
                }
            }
            etUnit1Value.addTextChangedListener(watcher)

            ivMax.setOnClickListener {
                /*    item.unit1Value += 1
                    etUnit1Value.setText(item.unit1Value.toString())
                    tvPrice.text =
                        formatter.format(item.price * item.unit1Value) + " ریال"
                    onQuantityChange(item, item.unit1Value)*/
            }

            ivMin.setOnClickListener {
                /* if (item.unit1Value > 0) {
                     item.unit1Value -= 1
                     etUnit1Value.setText(item.unit1Value.toString())
                     tvPrice.text =
                         formatter.format(item.price * item.unit1Value) + " ریال"
                 }
                 onQuantityChange(item, item.unit1Value)*/
            }

            binding.ivDelete.setOnClickListener {
                onDelete(item)
            }
            binding.clDiscount.setOnClickListener {
                showDiscountDialog(binding.root.context) { selectedDiscount ->
                    // نمایش در TextView
                    binding.tvDiscountPrice.text = "${selectedDiscount.percent}%"

                    // خط خوردن قیمت اصلی
                    binding.tvPrice.paintFlags =
                        binding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    // نمایش قیمت جدید (فرضی)
                    val discountedPrice =
                        item.price /*- (item.price * selectedDiscount.percent / 100)*/
                    binding.tvDiscountPrice.text = "${discountedPrice!!.toInt()} ریال"
                }
            }

        }
    }

    private fun showDiscountDialog(context: Context, onSelect: (Discount) -> Unit) {
        val dialog = Dialog(context)
        val binding = DialogSelectDiscountBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        val discountList = listOf(
            Discount(1, "5٪ تخفیف", 5.0),
            Discount(2, "10٪ تخفیف", 10.0),
            Discount(3, "20٪ تخفیف", 20.0),
            Discount(4, "30٪ تخفیف", 30.0)
        )

        var selected: Discount? = null
        val adapter = DiscountAdapter(discountList) { selected = it }

        // ⭐️ این خط ضروری بود
        binding.rvDiscounts.layoutManager = LinearLayoutManager(context)
        binding.rvDiscounts.adapter = adapter

        binding.btnSave.setOnClickListener {
            selected?.let { onSelect(it) }
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))

        val isLastItem = position == itemCount - 1
        holder.binding.vSeparator.visibility = if (isLastItem) View.GONE else View.VISIBLE
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<FactorDetailEntity>() {
    override fun areItemsTheSame(
        oldItem: FactorDetailEntity,
        newItem: FactorDetailEntity
    ): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(
        oldItem: FactorDetailEntity,
        newItem: FactorDetailEntity
    ): Boolean {
        return oldItem == newItem
    }
}
