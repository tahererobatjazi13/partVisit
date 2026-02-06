package com.partsystem.partvisitapp.feature.report_factor.online.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.clean
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorAttr
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.online.model.getPackingValueFormatted
import java.text.DecimalFormat

class OnlineOrderDetailAdapter :
    ListAdapter<ReportFactorDto, OnlineOrderDetailAdapter.OnlineOrderDetailViewHolder>(
        OnlineOrderDetailDiffCallback()
    ) {
    private val formatter = DecimalFormat("#,###,###,###")
    var backColorGift: Int = 0

    inner class OnlineOrderDetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {
            binding.clDelete.gone()
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

            if (item.isGift) {
                binding.root.setBackgroundColor(backColorGift)
            }
            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvUnitName.text = item.unitName

            if (item.packingName == null && item.packingCode == null) {
                // بدون بسته‌بندی
                tvPackingName.text = itemView.context.getString(R.string.label_no_packing)

            } else {
                // با بسته‌بندی
                tvPackingName.text = item.packingName
            }
            tvPackingValue.text = item.getPackingValueFormatted()

            tvRate1.text = formatter.format(item.rate1) + " ریال"
            tvUnitValue.text = item.unit1Value!!.clean()

            if (item.vat != null && item.vat > 0) {
                clVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"
                tvPriceAfterVat.text = formatter.format(item.priceAfterVat) + " م.بعداز مالیات"

            } else {
                clVat.gone()
            }

            if (item.discountPrice != null && item.discountPrice > 0) {
                clDiscountPrice.show()
                tvSumPrice.show()

                tvDiscountPrice.text = formatter.format(item.discountPrice) + " تخفیف"
                tvPriceAfterDiscount.text =
                    formatter.format(item.priceAfterDiscount) + " م.بعداز تخفیف"
                // قیمت اصلی → خط بخورد
                tvSumPrice.paintFlags = tvSumPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvSumPrice.setTextColor(Color.GRAY)
                tvSumPrice.text = formatter.format(item.price) + " ریال"

            } else {

                tvSumPrice.show()
                clDiscountPrice.gone()
                tvSumPrice.paintFlags = tvSumPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvSumPrice.text = formatter.format(item.price) + " ریال"
            }


            if (bindingAdapterPosition < itemCount - 1) {
                view.show()
            } else view.gone()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnlineOrderDetailViewHolder {
        val binding =
            ItemOrderDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return OnlineOrderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnlineOrderDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ReportFactorDto>) {
        submitList(data)
    }
}

class OnlineOrderDetailDiffCallback : DiffUtil.ItemCallback<ReportFactorDto>() {
    override fun areItemsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto): Boolean {
        return oldItem == newItem
    }
}
