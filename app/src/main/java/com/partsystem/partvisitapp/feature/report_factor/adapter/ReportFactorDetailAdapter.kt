package com.partsystem.partvisitapp.feature.report_factor.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemReportFactorDetailBinding
import java.text.DecimalFormat

class ReportFactorDetailAdapter :
    ListAdapter<ReportFactorDto, ReportFactorDetailAdapter.ReportFactorDetailViewHolder>(
        ReportFactorDetailDiffCallback()
    ) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class ReportFactorDetailViewHolder(val binding: ItemReportFactorDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {
            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_light)
                )
            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_dark)
                )
            }
            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvPackingName.text =item.packingName
            tvPackingValue.text = item.packingValue.toString()
            tvUnitName.text =item.unitName
            tvUnitValue.text = item.unit1Value.toString()

            if (item.discountPrice != null && item.discountPrice > 0) {
                tvDiscountPrice.show()
                tvPriceAfterDiscount.show()
                tvPrice.show()

                tvDiscountPrice.text = "م.تخفیف: " + formatter.format(item.discountPrice)
                tvPriceAfterDiscount.text = formatter.format(item.priceAfterDiscount)
                // قیمت اصلی → خط بخورد
                tvPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvPrice.setTextColor(Color.GRAY)
                tvPrice.text = formatter.format(item.price)

            } else {

                tvDiscountPrice.gone()
                tvPriceAfterDiscount.gone()

                tvPrice.paintFlags = tvPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvPrice.text = formatter.format(item.price)
            }


            if (bindingAdapterPosition < itemCount - 1) {
                view.show()
            } else view.gone()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportFactorDetailViewHolder {
        val binding =
            ItemReportFactorDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ReportFactorDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportFactorDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ReportFactorDto>) {
        submitList(data)
    }
}

class ReportFactorDetailDiffCallback : DiffUtil.ItemCallback<ReportFactorDto>() {
    override fun areItemsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto): Boolean {
        return oldItem == newItem
    }
}
