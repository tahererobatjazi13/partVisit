package com.partsystem.partvisitapp.feature.create_order.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.utils.extensions.gone
import java.text.DecimalFormat
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.getPackingValueFormatted


class OrderAdapter(
    private val onClickDialog: (FactorDetailUiModel) -> Unit = {},
    private val onDelete: (FactorDetailUiModel) -> Unit,
) : ListAdapter<FactorDetailUiModel, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val formatter = DecimalFormat("#,###,###,###")

    inner class OrderViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailUiModel) = with(binding) {
            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                )
            } else {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorRow)
                )
            }
            if (item.isGift == 1) {
                binding.ivDelete.gone()
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.yellow_FFEB3B
                    )
                )
            } else binding.ivDelete.show()

            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvPackingName.text = item.packingName
            tvPackingValue.text = item.getPackingValueFormatted()
            tvUnitName.text = item.unit1Name

            tvRate1.text = formatter.format(item.unit1Rate) + " ریال"
            val total = item.unit1Rate * item.unit1Value
            val displayUnit1 = item.unit1Value

            val unit1Text = if (displayUnit1 % 1 == 0.0) {
                item.unit1Value.toInt().toString()
            } else {
                item.unit1Value.toString()
            }
            tvUnitValue.text = unit1Text

            if (item.discountPrice > 0) {
                clDiscountPrice.show()
                tvSumPrice.show()

                tvDiscountPrice.text =
                    formatter.format(item.discountPrice) + " تخفیف"

                tvPriceAfterDiscount.text =
                    formatter.format(total - item.discountPrice) + " م.بعداز تخفیف"
                // قیمت اصلی → خط بخورد
                tvSumPrice.paintFlags = tvSumPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvSumPrice.setTextColor(Color.GRAY)
                tvSumPrice.text = formatter.format(total) + " ریال"

            } else {

                binding.clDiscountPrice.gone()
                tvSumPrice.show()
                tvSumPrice.paintFlags =
                    tvSumPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvSumPrice.text = formatter.format(total) + " ریال"
            }

            if (item.vat > 0) {
                tvVat.show()
                tvPriceAfterVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"
                tvPriceAfterVat.text =
                    formatter.format(item.vat + total) + " م.بعداز مالیات"
            } else {
                tvVat.gone()
                tvPriceAfterVat.gone()
            }
            ivDelete.setOnClickListener { onDelete(item) }
            itemView.setOnClickListener { onClickDialog(item) }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<FactorDetailUiModel>() {
    override fun areItemsTheSame(old: FactorDetailUiModel, new: FactorDetailUiModel) =
        old.factorId == new.factorId

    override fun areContentsTheSame(old: FactorDetailUiModel, new: FactorDetailUiModel) =
        old == new
}