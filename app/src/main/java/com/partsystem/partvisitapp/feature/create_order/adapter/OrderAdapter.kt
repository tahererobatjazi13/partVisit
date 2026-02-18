package com.partsystem.partvisitapp.feature.create_order.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.utils.extensions.gone
import java.text.DecimalFormat
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.clean
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorAttr
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.getPackingValueFormatted


class OrderAdapter(
    private val onClickDialog: (FactorDetailUiModel) -> Unit = {},
    private val onDelete: (FactorDetailUiModel) -> Unit,
) : ListAdapter<FactorDetailUiModel, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {
    var backColorGift: Int = 0
    private var isOrderCompleted = false // فلگ کنترل وضعیت تکمیل سفارش

    private val formatter = DecimalFormat("#,###,###,###")

    @SuppressLint("NotifyDataSetChanged")
    fun setOrderCompleted(completed: Boolean) {
        isOrderCompleted = completed
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailUiModel) = with(binding) {
            val context = binding.root.context

            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                )
            } else {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorRow)
                )
            }

            backColorGift = getColorAttr(context, R.attr.colorGift)

            if (isOrderCompleted) {
                // سطل آشغال در حالت تکمیل سفارش همیشه مخفی است
                binding.ivDelete.gone()
                if (item.isGift == 1) {
                    binding.root.setBackgroundColor(backColorGift)
                }
            } else {
                // در حالت عادی، فقط ردیف‌های غیرهدیه قابل حذف هستند
                if (item.isGift == 1) {
                    binding.ivDelete.gone()
                    binding.root.setBackgroundColor(backColorGift)
                } else {
                    binding.ivDelete.show()
                }
            }

            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvUnitName.text = item.unit1Name

            if (item.packingId == 0) {
                // بدون بسته‌بندی
                tvPackingName.text = itemView.context.getString(R.string.label_no_packing)
            } else {
                // با بسته‌بندی
                tvPackingName.text = item.packingName
            }
            tvPackingValue.text = item.getPackingValueFormatted()

            tvRate1.text = formatter.format(item.unit1Rate) + " ریال"
            tvUnitValue.text = item.unit1Value.clean()
            val total = item.unit1Rate * item.unit1Value

            if (item.vat > 0) {

                clVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"

                if (item.discountPrice > 0) {
                    tvPriceAfterVat.text =
                        formatter.format((total - item.discountPrice) + item.vat) + " م.بعداز مالیات"
                } else {
                    tvPriceAfterVat.text =
                        formatter.format(item.vat + total) + " م.بعداز مالیات"
                }

            } else {
                clVat.gone()
            }

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
            ivDelete.setOnClickListener {
                if (!isOrderCompleted) {
                    onDelete(item)
                }
            }
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