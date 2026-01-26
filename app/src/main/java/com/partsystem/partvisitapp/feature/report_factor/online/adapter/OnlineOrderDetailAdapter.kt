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
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.online.model.getPackingValueFormatted
import java.text.DecimalFormat

class OnlineOrderDetailAdapter :
    ListAdapter<ReportFactorDto, OnlineOrderDetailAdapter.OnlineOrderDetailViewHolder>(
        OnlineOrderDetailDiffCallback()
    ) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class OnlineOrderDetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {
            binding.clDelete.gone()

            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                )
            } else {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorRow)
                )
            }
            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvPackingName.text = item.packingName
            tvPackingValue.text = item.getPackingValueFormatted()
            tvUnitName.text = item.unitName
            tvRate1.text = formatter.format(item.rate1) + " ریال"
            tvUnitValue.text = item.unit1Value!!.clean()

            if (item.discountPrice != null && item.discountPrice > 0) {
                tvDiscountPrice.show()
                tvPriceAfterDiscount.show()
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
                tvDiscountPrice.gone()
                tvPriceAfterDiscount.gone()
                tvSumPrice.paintFlags = tvSumPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvSumPrice.text = formatter.format(item.price) + " ریال"
            }

            if (item.vat != null && item.vat > 0) {
                tvVat.show()
                tvPriceAfterVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"
                tvPriceAfterVat.text = formatter.format(item.priceAfterVat) + " م.بعداز مالیات"

            } else {
                tvVat.gone()
                tvPriceAfterVat.gone()
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
